package com.example.watchlog.service;

import com.example.watchlog.api.ApiException;
import com.example.watchlog.api.dto.WorkDtos.AvailabilityRequest;
import com.example.watchlog.api.dto.WorkDtos.AvailabilityUpdateRequest;
import com.example.watchlog.api.dto.WorkDtos.StreamingOptionResponse;
import com.example.watchlog.api.dto.WorkDtos.WorkRequest;
import com.example.watchlog.api.dto.WorkDtos.WorkResponse;
import com.example.watchlog.domain.StreamingService;
import com.example.watchlog.domain.Work;
import com.example.watchlog.domain.WorkStreamingAvailability;
import com.example.watchlog.repository.StreamingServiceRepository;
import com.example.watchlog.repository.ViewingHistoryRepository;
import com.example.watchlog.repository.WorkRepository;
import com.example.watchlog.repository.WorkStreamingAvailabilityRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkService {
    private final WorkRepository workRepository;
    private final StreamingServiceRepository serviceRepository;
    private final WorkStreamingAvailabilityRepository availabilityRepository;
    private final ViewingHistoryRepository historyRepository;

    public WorkService(WorkRepository workRepository, StreamingServiceRepository serviceRepository,
                       WorkStreamingAvailabilityRepository availabilityRepository,
                       ViewingHistoryRepository historyRepository) {
        this.workRepository = workRepository;
        this.serviceRepository = serviceRepository;
        this.availabilityRepository = availabilityRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkResponse> findAll() {
        return workRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WorkResponse findOne(Long id) {
        return toResponse(requireWork(id));
    }

    public WorkResponse create(WorkRequest request) {
        Work work = new Work(request.title().trim(), request.description().trim(), request.type(),
                request.releaseYear(), request.genre().trim(), clean(request.director()), clean(request.imageUrl()));
        return toResponse(workRepository.save(work));
    }

    public WorkResponse update(Long id, WorkRequest request) {
        Work work = requireWork(id);
        work.update(request.title().trim(), request.description().trim(), request.type(), request.releaseYear(),
                request.genre().trim(), clean(request.director()), clean(request.imageUrl()));
        return toResponse(work);
    }

    public void delete(Long id) {
        Work work = requireWork(id);
        historyRepository.deleteByWorkId(id);
        availabilityRepository.deleteByWorkId(id);
        workRepository.delete(work);
    }

    public StreamingOptionResponse addAvailability(Long workId, AvailabilityRequest request) {
        Work work = requireWork(workId);
        StreamingService streamingService = serviceRepository.findById(request.streamingServiceId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "STREAMING_SERVICE_NOT_FOUND", "指定された配信サービスが見つかりません"));
        if (availabilityRepository.existsByWorkIdAndStreamingServiceId(workId, request.streamingServiceId())) {
            throw new ApiException(HttpStatus.CONFLICT, "AVAILABILITY_ALREADY_EXISTS", "この作品には同じ配信サービスが登録済みです");
        }
        return toOption(availabilityRepository.save(new WorkStreamingAvailability(
                work, streamingService, clean(request.viewingUrl()), request.availabilityType())));
    }

    public StreamingOptionResponse updateAvailability(Long workId, Long availabilityId, AvailabilityUpdateRequest request) {
        requireWork(workId);
        WorkStreamingAvailability availability = requireAvailability(workId, availabilityId);
        availability.update(clean(request.viewingUrl()), request.availabilityType());
        return toOption(availability);
    }

    public void deleteAvailability(Long workId, Long availabilityId) {
        requireWork(workId);
        availabilityRepository.delete(requireAvailability(workId, availabilityId));
    }

    public Work requireWork(Long id) {
        return workRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "WORK_NOT_FOUND", "指定された作品が見つかりません"));
    }

    private WorkStreamingAvailability requireAvailability(Long workId, Long availabilityId) {
        WorkStreamingAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "AVAILABILITY_NOT_FOUND", "指定された配信情報が見つかりません"));
        if (!availability.getWork().getId().equals(workId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "AVAILABILITY_NOT_FOUND", "指定された配信情報が見つかりません");
        }
        return availability;
    }

    private WorkResponse toResponse(Work work) {
        List<StreamingOptionResponse> options = availabilityRepository
                .findByWorkIdOrderByStreamingServiceName(work.getId()).stream().map(this::toOption).toList();
        return new WorkResponse(work.getId(), work.getTitle(), work.getDescription(), work.getType(),
                work.getReleaseYear(), work.getGenre(), work.getDirector(), work.getImageUrl(),
                work.getExternalSource(), work.getExternalId(), work.getExternalMediaType(),
                work.getExternalUpdatedAt(), options,
                work.getCreatedAt(), work.getUpdatedAt());
    }

    private StreamingOptionResponse toOption(WorkStreamingAvailability availability) {
        StreamingService service = availability.getStreamingService();
        return new StreamingOptionResponse(availability.getId(), service.getId(), service.getName(),
                service.getServiceUrl(), service.getLogoUrl(), availability.getViewingUrl(),
                availability.getAvailabilityType());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
