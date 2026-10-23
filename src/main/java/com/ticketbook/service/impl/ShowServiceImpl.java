package com.ticketbook.service.impl;

import com.ticketbook.dto.SeatAvailabilityResponse;
import com.ticketbook.dto.ShowRequest;
import com.ticketbook.dto.ShowResponse;
import com.ticketbook.entity.Movie;
import com.ticketbook.entity.Screen;
import com.ticketbook.entity.Seat;
import com.ticketbook.entity.Show;
import com.ticketbook.exception.MovieNotFoundException;
import com.ticketbook.exception.ScreenNotFoundException;
import com.ticketbook.exception.ShowConflictException;
import com.ticketbook.exception.ShowNotFoundException;
import com.ticketbook.mapper.ShowMapper;
import com.ticketbook.repository.BookingSeatRepository;
import com.ticketbook.repository.MovieRepository;
import com.ticketbook.repository.ScreenRepository;
import com.ticketbook.repository.SeatRepository;
import com.ticketbook.repository.ShowRepository;
import com.ticketbook.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowMapper showMapper;

    @Override
    @Transactional
    public ShowResponse create(ShowRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + request.getMovieId()));

        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ScreenNotFoundException("Screen not found with id: " + request.getScreenId()));

        LocalTime endTime = request.getStartTime().plusMinutes(movie.getDuration());

        List<Show> conflicts = showRepository.findOverlappingShows(
                screen.getId(), request.getShowDate(), request.getStartTime(), endTime);
        if (!conflicts.isEmpty()) {
            throw new ShowConflictException(
                    "Screen " + screen.getScreenName() + " already has a show scheduled in that time range");
        }

        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(endTime);
        show.setTicketPrice(request.getTicketPrice());
        show.setStatus("SCHEDULED");

        Show saved = showRepository.save(show);
        return showMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ShowResponse update(Long id, ShowRequest request) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException("Show not found with id: " + id));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + request.getMovieId()));
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ScreenNotFoundException("Screen not found with id: " + request.getScreenId()));

        LocalTime endTime = request.getStartTime().plusMinutes(movie.getDuration());

        List<Show> conflicts = showRepository.findOverlappingShows(
                screen.getId(), request.getShowDate(), request.getStartTime(), endTime)
                .stream().filter(s -> !s.getId().equals(id)).toList();
        if (!conflicts.isEmpty()) {
            throw new ShowConflictException(
                    "Screen " + screen.getScreenName() + " already has a show scheduled in that time range");
        }

        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(endTime);
        show.setTicketPrice(request.getTicketPrice());

        return showMapper.toResponse(showRepository.save(show));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException("Show not found with id: " + id));
        show.setStatus("CANCELLED");
        showRepository.save(show);
    }

    @Override
    @Transactional(readOnly = true)
    public ShowResponse getById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException("Show not found with id: " + id));
        return showMapper.toResponse(show);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShowResponse> search(Long movieId, Long theatreId, LocalDate date, Pageable pageable) {
        return showRepository.search(movieId, theatreId, date, pageable)
                .map(showMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatAvailabilityResponse> getSeatAvailability(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found with id: " + showId));

        List<Seat> allSeats = seatRepository.findByScreenId(show.getScreen().getId());
        Set<Long> bookedSeatIds = new HashSet<>(bookingSeatRepository.findBookedSeatIdsByShowId(showId));

        return allSeats.stream()
                .map(seat -> new SeatAvailabilityResponse(
                        seat.getId(),
                        seat.getSeatNumber(),
                        seat.getRowNumber(),
                        seat.getSeatType(),
                        bookedSeatIds.contains(seat.getId()) ? "BOOKED" : "AVAILABLE"
                ))
                .toList();
    }
}
