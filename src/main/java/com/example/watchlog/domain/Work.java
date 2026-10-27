package com.example.watchlog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "works")
public class Work extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WorkType type;

    @Column(nullable = false)
    private Integer releaseYear;

    @Column(nullable = false, length = 100)
    private String genre;

    @Column(length = 150)
    private String director;

    @Column(length = 1000)
    private String imageUrl;

    protected Work() {}

    public Work(String title, String description, WorkType type, Integer releaseYear, String genre,
                String director, String imageUrl) {
        update(title, description, type, releaseYear, genre, director, imageUrl);
    }

    public void update(String title, String description, WorkType type, Integer releaseYear, String genre,
                       String director, String imageUrl) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.director = director;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public WorkType getType() { return type; }
    public Integer getReleaseYear() { return releaseYear; }
    public String getGenre() { return genre; }
    public String getDirector() { return director; }
    public String getImageUrl() { return imageUrl; }
}
