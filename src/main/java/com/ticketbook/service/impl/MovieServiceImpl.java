package com.ticketbook.service.impl;

import com.ticketbook.dto.MovieRequest;
import com.ticketbook.dto.MovieResponse;
import com.ticketbook.entity.Movie;
import com.ticketbook.exception.MovieNotFoundException;
import com.ticketbook.mapper.MovieMapper;
import com.ticketbook.repository.MovieRepository;
import com.ticketbook.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    @Override
    @Transactional
    public MovieResponse create(MovieRequest request) {
        Movie movie = movieMapper.toEntity(request);
        Movie saved = movieRepository.save(movie);
        return movieMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MovieResponse update(Long id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + id));
        movieMapper.updateEntity(movie, request);
        Movie updated = movieRepository.save(movie);
        return movieMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + id));
        movie.setStatus("INACTIVE");
        movieRepository.save(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + id));
        return movieMapper.toResponse(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> search(String keyword, String genre, String language, Pageable pageable) {
        return movieRepository.search(keyword, genre, language, pageable)
                .map(movieMapper::toResponse);
    }
}
