package com.ticketbook.service;

import com.ticketbook.dto.MovieRequest;
import com.ticketbook.dto.MovieResponse;
import com.ticketbook.entity.Movie;
import com.ticketbook.exception.MovieNotFoundException;
import com.ticketbook.mapper.MovieMapper;
import com.ticketbook.repository.MovieRepository;
import com.ticketbook.service.impl.MovieServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock private MovieRepository movieRepository;
    @Mock private MovieMapper movieMapper;

    @InjectMocks
    private MovieServiceImpl movieService;

    @Test
    void create_savesAndReturnsMappedResponse() {
        MovieRequest request = new MovieRequest();
        request.setTitle("Inception");
        request.setDuration(148);

        Movie entity = new Movie();
        Movie saved = new Movie();
        saved.setId(1L);
        saved.setTitle("Inception");

        MovieResponse expectedResponse = new MovieResponse(1L, "Inception", null, null, null, 148, null, "ACTIVE");

        when(movieMapper.toEntity(request)).thenReturn(entity);
        when(movieRepository.save(entity)).thenReturn(saved);
        when(movieMapper.toResponse(saved)).thenReturn(expectedResponse);

        MovieResponse result = movieService.create(request);

        assertThat(result.getTitle()).isEqualTo("Inception");
        verify(movieRepository).save(entity);
    }

    @Test
    void update_movieNotFound_throwsException() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        MovieRequest request = new MovieRequest();

        assertThatThrownBy(() -> movieService.update(99L, request))
                .isInstanceOf(MovieNotFoundException.class);

        verify(movieRepository, never()).save(any());
    }

    @Test
    void delete_softDeletesBySettingStatusInactive() {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setStatus("ACTIVE");

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        movieService.delete(1L);

        assertThat(movie.getStatus()).isEqualTo("INACTIVE");
        verify(movieRepository).save(movie);
    }

    @Test
    void getById_notFound_throwsMovieNotFoundException() {
        when(movieRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getById(404L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("404");
    }
}
