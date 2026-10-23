package com.ticketbook.integration;

import com.ticketbook.dto.BookingRequest;
import com.ticketbook.entity.Movie;
import com.ticketbook.entity.Payment;
import com.ticketbook.entity.PaymentMethod;
import com.ticketbook.entity.PaymentStatus;
import com.ticketbook.entity.Role;
import com.ticketbook.entity.Screen;
import com.ticketbook.entity.Seat;
import com.ticketbook.entity.SeatType;
import com.ticketbook.entity.Show;
import com.ticketbook.entity.Theatre;
import com.ticketbook.entity.User;
import com.ticketbook.exception.PaymentFailedException;
import com.ticketbook.exception.SeatAlreadyBookedException;
import com.ticketbook.repository.BookingSeatRepository;
import com.ticketbook.repository.MovieRepository;
import com.ticketbook.repository.ScreenRepository;
import com.ticketbook.repository.SeatRepository;
import com.ticketbook.repository.ShowRepository;
import com.ticketbook.repository.TheatreRepository;
import com.ticketbook.repository.UserRepository;
import com.ticketbook.service.BookingService;
import com.ticketbook.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class BookingConcurrencyIntegrationTest {

    @Autowired private BookingService bookingService;
    @Autowired private UserRepository userRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private TheatreRepository theatreRepository;
    @Autowired private ScreenRepository screenRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private ShowRepository showRepository;
    @Autowired private BookingSeatRepository bookingSeatRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // Payment is mocked so the test is deterministic: the real simulation fails ~10%
    // of the time, and a failed payment rolls back and frees the seat for the next
    // waiting thread -- correct behaviour, but it makes exact counts random.
    @MockBean private PaymentService paymentService;

    private Show contestedShow;
    private Seat contestedSeat;

    @BeforeEach
    void setUp() {
        when(paymentService.processPayment(any(), any(), any())).thenAnswer(inv -> {
            Payment p = new Payment();
            p.setTransactionId("TXN-TEST-" + UUID.randomUUID());
            p.setAmount(inv.getArgument(1));
            p.setPaymentMethod(inv.getArgument(2));
            p.setPaymentStatus(PaymentStatus.SUCCESS);
            return p;
        });

        Theatre theatre = new Theatre();
        theatre.setName("Test Theatre");
        theatre.setLocation("Loc");
        theatre.setCity("City");
        theatre.setStatus("ACTIVE");
        theatre = theatreRepository.save(theatre);

        Screen screen = new Screen();
        screen.setScreenName("Screen 1");
        screen.setCapacity(50);
        screen.setTheatre(theatre);
        screen = screenRepository.save(screen);

        Seat seat = new Seat();
        seat.setSeatNumber("1");
        seat.setRowNumber("A");
        seat.setSeatType(SeatType.REGULAR);
        seat.setScreen(screen);
        contestedSeat = seatRepository.save(seat);

        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setDuration(120);
        movie.setStatus("ACTIVE");
        movie = movieRepository.save(movie);

        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(LocalDate.now().plusDays(1));
        show.setStartTime(LocalTime.of(19, 0));
        show.setEndTime(LocalTime.of(21, 0));
        show.setTicketPrice(BigDecimal.valueOf(200));
        show.setStatus("SCHEDULED");
        contestedShow = showRepository.save(show);
    }

    @Test
    void tenConcurrentRequestsForSameSeat_exactlyOneSucceeds() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger unexpectedCount = new AtomicInteger(0);
        String runId = UUID.randomUUID().toString().substring(0, 8);

        for (int i = 0; i < threadCount; i++) {
            final int userIndex = i;
            executor.submit(() -> {
                try {
                    User user = new User();
                    user.setName("User" + userIndex);
                    user.setEmail("user" + userIndex + "-" + runId + "@test.com");
                    user.setPassword(passwordEncoder.encode("password"));
                    user.setRole(Role.USER);
                    user.setStatus("ACTIVE");
                    userRepository.save(user);

                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of()));

                    readyLatch.countDown();
                    startLatch.await();

                    BookingRequest request = new BookingRequest();
                    request.setShowId(contestedShow.getId());
                    request.setSeatIds(List.of(contestedSeat.getId()));
                    request.setPaymentMethod(PaymentMethod.UPI);

                    bookingService.createBooking(request);
                    successCount.incrementAndGet();

                } catch (SeatAlreadyBookedException e) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    unexpectedCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    SecurityContextHolder.clearContext();
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(unexpectedCount.get()).as("No unexpected exceptions").isZero();
        assertThat(successCount.get()).as("Exactly one booking succeeds").isEqualTo(1);
        assertThat(conflictCount.get()).as("Everyone else gets SeatAlreadyBookedException")
                .isEqualTo(threadCount - 1);

        // Verify the database itself, not just the counters: the seat is held exactly once
        assertThat(bookingSeatRepository.findBookedSeatIdsByShowId(contestedShow.getId()))
                .containsExactly(contestedSeat.getId());
    }

    @Test
    void paymentFailure_rollsBackBookingAndReleasesSeat() {
        doReturn(null).when(paymentService).processPayment(any(), any(), any());

        User user = new User();
        user.setName("Rollback User");
        user.setEmail("rollback-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(Role.USER);
        user.setStatus("ACTIVE");
        userRepository.save(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of()));

        BookingRequest request = new BookingRequest();
        request.setShowId(contestedShow.getId());
        request.setSeatIds(List.of(contestedSeat.getId()));
        request.setPaymentMethod(PaymentMethod.CARD);

        try {
            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(PaymentFailedException.class);
        } finally {
            SecurityContextHolder.clearContext();
        }

        // The whole transaction rolled back: no booking_seats row, so the seat is free again
        assertThat(bookingSeatRepository.findBookedSeatIdsByShowId(contestedShow.getId())).isEmpty();
    }
}
