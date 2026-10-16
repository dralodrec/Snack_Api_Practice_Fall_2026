package com.movieapi.movie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public String saveMovie(Movie movie) {
        movieRepository.save(movie);
        return "Movie saved successfully";
    }

    public Optional<Movie> getMovieById(Long movieId) {
        return movieRepository.findById(movieId);
    }


    public Iterable<Movie> getAllMovies() {
        return movieRepository.findAll();

    }

    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }

    public Iterable<Movie> findByGenre(String genre) {
        return movieRepository.findMovieByGenre(genre);
    }
}
