package com.example.watchlog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "streaming_services", uniqueConstraints = @UniqueConstraint(name = "uk_service_name", columnNames = "name"))
public class StreamingService extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 1000)
    private String serviceUrl;

    @Column(length = 1000)
    private String logoUrl;

    protected StreamingService() {}

    public StreamingService(String name, String serviceUrl, String logoUrl) {
        update(name, serviceUrl, logoUrl);
    }

    public void update(String name, String serviceUrl, String logoUrl) {
        this.name = name;
        this.serviceUrl = serviceUrl;
        this.logoUrl = logoUrl;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getServiceUrl() { return serviceUrl; }
    public String getLogoUrl() { return logoUrl; }
}
