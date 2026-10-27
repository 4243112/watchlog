package com.example.watchlog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "work_streaming_availabilities", uniqueConstraints = @UniqueConstraint(
        name = "uk_work_service", columnNames = {"work_id", "streaming_service_id"}))
public class WorkStreamingAvailability extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "streaming_service_id", nullable = false)
    private StreamingService streamingService;

    @Column(length = 1000)
    private String viewingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AvailabilityType availabilityType;

    protected WorkStreamingAvailability() {}

    public WorkStreamingAvailability(Work work, StreamingService streamingService, String viewingUrl,
                                     AvailabilityType availabilityType) {
        this.work = work;
        this.streamingService = streamingService;
        update(viewingUrl, availabilityType);
    }

    public void update(String viewingUrl, AvailabilityType availabilityType) {
        this.viewingUrl = viewingUrl;
        this.availabilityType = availabilityType;
    }

    public Long getId() { return id; }
    public Work getWork() { return work; }
    public StreamingService getStreamingService() { return streamingService; }
    public String getViewingUrl() { return viewingUrl; }
    public AvailabilityType getAvailabilityType() { return availabilityType; }
}
