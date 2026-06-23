package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.dto.request.MovieRequest;
import com.amritesh.moviebooking.dto.response.MovieResponse;
import com.amritesh.moviebooking.entity.Movie;
import com.amritesh.moviebooking.exception.ResourceNotFoundException;
import com.amritesh.moviebooking.mapper.MovieMapper;
import com.amritesh.moviebooking.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    public MovieService(MovieRepository movieRepository, MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
    }

    @Transactional
    public MovieResponse create(MovieRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.title());
        movie.setLanguage(request.language());
        movie.setDurationMins(request.durationMins());
        movie.setCertification(request.certification());
        return movieMapper.toResponse(movieRepository.save(movie));
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> findAll() {
        return movieRepository.findAll().stream().map(movieMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MovieResponse findById(Long id) {
        return movieMapper.toResponse(movieRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Movie", id)));
    }
}
