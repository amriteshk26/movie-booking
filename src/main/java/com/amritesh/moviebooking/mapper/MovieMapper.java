package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.MovieResponse;
import com.amritesh.moviebooking.entity.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    public MovieResponse toResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getLanguage(),
                movie.getDurationMins(),
                movie.getCertification()
        );
    }
}
