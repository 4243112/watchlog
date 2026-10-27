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
import java.time.LocalDate;

@Entity
@Table(name = "viewing_histories", uniqueConstraints = @UniqueConstraint(
        name = "uk_history_user_work", columnNames = {"user_id", "work_id"}))
public class ViewingHistory extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ViewingStatus status;

    private LocalDate watchedAt;
    private Integer rating;

    @Column(length = 2000)
    private String comment;

    protected ViewingHistory() {}

    public ViewingHistory(AppUser user, Work work, ViewingStatus status, LocalDate watchedAt,
                          Integer rating, String comment) {
        this.user = user;
        this.work = work;
        update(status, watchedAt, rating, comment);
    }

    public void update(ViewingStatus status, LocalDate watchedAt, Integer rating, String comment) {
        this.status = status;
        this.watchedAt = watchedAt;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public Work getWork() { return work; }
    public ViewingStatus getStatus() { return status; }
    public LocalDate getWatchedAt() { return watchedAt; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
}
