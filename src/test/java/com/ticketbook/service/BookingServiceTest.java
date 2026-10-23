package com.ticketbook.service;

import com.ticketbook.dto.BookingRequest;
import com.ticketbook.entity.Movie;
import com.ticketbook.entity.PaymentMethod;
import com.ticketbook.entity.Screen;
import com.ticketbook.entity.Seat;
import com.ticketbook.entity.Show;
import com.ticketbook.entity.Theatre;
import com.ticketbook.entity.User;
import com.ticketbook.exception.SeatAlreadyBookedException;
import com.ticketbook.mapper.BookingMapper;
import com.ticketbook.repository.BookingRepository;
import com.ticketbook.repository.BookingSeatRepository;
import com.ticketbook.repository.PaymentRepository;
import com.ticketbook.repository.SeatRepository;
import com.ticketbook.repository.ShowRepository;
import com.ticketbook.repository.UserRepository;
import com.ticketbook.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private ShowRepository showRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private BookingSeatRepository bookingSeatRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentService paymentService;
    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private Show show;
    private Screen screen;
    private Seat seat10;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("jagad@example.com");

        Theatre theatre = new Theatre();
        theatre.setId(1L);

        screen = new Screen();
        screen.setId(1L);
        screen.setTheatre(theatre);

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");
        movie.setDuration(148);

        show = new Show();
        show.setId(1L);
        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(LocalDate.now().plusDays(1));
        show.setStartTime(LocalTime.of(19, 0));
        show.setTicketPrice(BigDecimal.valueOf(250));
        show.setStatus("SCHEDULED");

        seat10 = new Seat();
        seat10.setId(10L);
        seat10.setScreen(screen);
        seat10.setSeatNumber("10");
        seat10.setRowNumber("A");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of()));
        when(userRepository.findByEmail("jagad@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void createBooking_seatAlreadyBooked_throwsSeatAlreadyBookedException() {
        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(List.of(10L));
        request.setPaymentMethod(PaymentMethod.UPI);

        when(showRepository.findById(1L)).thenReturn(Optional.of(show));
        when(seatRepository.findByIdsForUpdate(List.of(10L))).thenReturn(List.of(seat10));
        when(bookingSeatRepository.findBookedSeatIdsByShowId(1L)).thenReturn(List.of(10L));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(SeatAlreadyBookedException.class)
                .hasMessageContaining("10");

        verify(bookingRepository, never()).save(any());
        verify(paymentService, never()).processPayment(any(), any(), any());
    }

    @Test
    void createBooking_showNotScheduled_throwsInvalidBookingException() {
        show.setStatus("CANCELLED");

        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(List.of(10L));
        request.setPaymentMethod(PaymentMethod.UPI);

        when(showRepository.findById(1L)).thenReturn(Optional.of(show));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .hasMessageContaining("no longer available");

        verify(seatRepository, never()).findByIdsForUpdate(any());
    }

    @Test
    void createBooking_seatNotOnShowsScreen_throwsInvalidBookingException() {
        Screen otherScreen = new Screen();
        otherScreen.setId(999L);
        Seat wrongScreenSeat = new Seat();
        wrongScreenSeat.setId(20L);
        wrongScreenSeat.setScreen(otherScreen);

        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(List.of(20L));
        request.setPaymentMethod(PaymentMethod.UPI);

        when(showRepository.findById(1L)).thenReturn(Optional.of(show));
        when(seatRepository.findByIdsForUpdate(List.of(20L))).thenReturn(List.of(wrongScreenSeat));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .hasMessageContaining("do not belong to this show's screen");
    }

    @Test
    void createBooking_duplicateSeatIdsInRequest_throwsInvalidBookingException() {
        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(List.of(10L, 10L));
        request.setPaymentMethod(PaymentMethod.UPI);

        when(showRepository.findById(1L)).thenReturn(Optional.of(show));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .hasMessageContaining("Duplicate seat IDs");
    }
}
