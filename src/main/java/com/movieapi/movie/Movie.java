package com.movieapi.movie;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movieId;
    private String title;
    private String genre;
    private String releaseDate;

    public Movie() {
    }

    public Movie(String title, String genre, String releaseDate) {
        this.title = title;
        this.genre = genre;
        this.releaseDate = releaseDate;
    }

    public Movie(Long movieId, String title, String genre, String releaseDate) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.releaseDate = releaseDate;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
}
