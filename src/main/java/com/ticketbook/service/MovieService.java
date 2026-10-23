package com.ticketbook.service;

import com.ticketbook.dto.MovieRequest;
import com.ticketbook.dto.MovieResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovieService {
    MovieResponse create(MovieRequest request);
    MovieResponse update(Long id, MovieRequest request);
    void delete(Long id);
    MovieResponse getById(Long id);
    Page<MovieResponse> search(String keyword, String genre, String language, Pageable pageable);
}
