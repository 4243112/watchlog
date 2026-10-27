package com.example.watchlog.service;

import com.example.watchlog.api.ApiException;
import com.example.watchlog.api.dto.StreamingServiceDtos.StreamingServiceRequest;
import com.example.watchlog.api.dto.StreamingServiceDtos.StreamingServiceResponse;
import com.example.watchlog.domain.StreamingService;
import com.example.watchlog.repository.StreamingServiceRepository;
import com.example.watchlog.repository.WorkStreamingAvailabilityRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StreamingCatalogService {
    private final StreamingServiceRepository serviceRepository;
    private final WorkStreamingAvailabilityRepository availabilityRepository;

    public StreamingCatalogService(StreamingServiceRepository serviceRepository,
                                   WorkStreamingAvailabilityRepository availabilityRepository) {
        this.serviceRepository = serviceRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Transactional(readOnly = true)
    public List<StreamingServiceResponse> findAll() {
        return serviceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StreamingServiceResponse findOne(Long id) {
        return toResponse(requireService(id));
    }

    public StreamingServiceResponse create(StreamingServiceRequest request) {
        if (serviceRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "STREAMING_SERVICE_ALREADY_EXISTS", "同名の配信サービスがすでに登録されています");
        }
        StreamingService service = serviceRepository.save(new StreamingService(
                request.name().trim(), request.serviceUrl().trim(), clean(request.logoUrl())));
        return toResponse(service);
    }

    public StreamingServiceResponse update(Long id, StreamingServiceRequest request) {
        StreamingService service = requireService(id);
        service.update(request.name().trim(), request.serviceUrl().trim(), clean(request.logoUrl()));
        return toResponse(service);
    }

    public void delete(Long id) {
        StreamingService service = requireService(id);
        availabilityRepository.deleteByStreamingServiceId(id);
        serviceRepository.delete(service);
    }

    private StreamingService requireService(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "STREAMING_SERVICE_NOT_FOUND", "指定された配信サービスが見つかりません"));
    }

    private StreamingServiceResponse toResponse(StreamingService service) {
        return new StreamingServiceResponse(service.getId(), service.getName(), service.getServiceUrl(),
                service.getLogoUrl(), service.getCreatedAt(), service.getUpdatedAt());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
