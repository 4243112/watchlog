package com.example.watchlog.api;

import com.example.watchlog.api.dto.StreamingServiceDtos.StreamingServiceRequest;
import com.example.watchlog.api.dto.StreamingServiceDtos.StreamingServiceResponse;
import com.example.watchlog.service.StreamingCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/streaming-services")
public class StreamingServiceController {
    private final StreamingCatalogService catalogService;

    public StreamingServiceController(StreamingCatalogService catalogService) { this.catalogService = catalogService; }

    @GetMapping
    public List<StreamingServiceResponse> findAll() { return catalogService.findAll(); }

    @PostMapping
    public ResponseEntity<StreamingServiceResponse> create(@Valid @RequestBody StreamingServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.create(request));
    }

    @GetMapping("/{id}")
    public StreamingServiceResponse findOne(@PathVariable Long id) { return catalogService.findOne(id); }

    @PutMapping("/{id}")
    public StreamingServiceResponse update(@PathVariable Long id,
                                           @Valid @RequestBody StreamingServiceRequest request) {
        return catalogService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        catalogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
