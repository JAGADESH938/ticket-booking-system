package com.ticketbook.service.impl;

import com.ticketbook.dto.BookingRequest;
import com.ticketbook.dto.BookingResponse;
import com.ticketbook.entity.Booking;
import com.ticketbook.entity.BookingSeat;
import com.ticketbook.entity.BookingStatus;
import com.ticketbook.entity.Payment;
import com.ticketbook.entity.Role;
import com.ticketbook.entity.Seat;
import com.ticketbook.entity.Show;
import com.ticketbook.entity.User;
import com.ticketbook.exception.BookingNotFoundException;
import com.ticketbook.exception.InvalidBookingException;
import com.ticketbook.exception.PaymentFailedException;
import com.ticketbook.exception.SeatAlreadyBookedException;
import com.ticketbook.exception.ShowNotFoundException;
import com.ticketbook.exception.UnauthorizedOperationException;
import com.ticketbook.exception.UserNotFoundException;
import com.ticketbook.mapper.BookingMapper;
import com.ticketbook.repository.BookingRepository;
import com.ticketbook.repository.BookingSeatRepository;
import com.ticketbook.repository.PaymentRepository;
import com.ticketbook.repository.SeatRepository;
import com.ticketbook.repository.ShowRepository;
import com.ticketbook.repository.UserRepository;
import com.ticketbook.service.BookingService;
import com.ticketbook.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {

        // 1. Identify the authenticated user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        // 2. Validate the show
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ShowNotFoundException("Show not found with id: " + request.getShowId()));

        if (!"SCHEDULED".equals(show.getStatus())) {
            throw new InvalidBookingException("This show is no longer available for booking");
        }
        if (show.getShowDate().isBefore(LocalDate.now())
                || (show.getShowDate().isEqual(LocalDate.now())
                    && show.getStartTime().isBefore(LocalTime.now()))) {
            throw new InvalidBookingException("Cannot book a show that has already started or passed");
        }

        // 3. Sort + validate for duplicates, then LOCK the requested seats
        List<Long> sortedSeatIds = request.getSeatIds().stream().sorted().toList();

        long distinctCount = sortedSeatIds.stream().distinct().count();
        if (distinctCount != sortedSeatIds.size()) {
            throw new InvalidBookingException("Duplicate seat IDs in booking request");
        }

        // PESSIMISTIC_WRITE lock: blocks any other transaction from locking
        // these same seat rows until THIS transaction commits or rolls back.
        List<Seat> lockedSeats = seatRepository.findByIdsForUpdate(sortedSeatIds);

        if (lockedSeats.size() != sortedSeatIds.size()) {
            throw new InvalidBookingException("One or more selected seats do not exist");
        }

        Long expectedScreenId = show.getScreen().getId();
        boolean allSeatsOnScreen = lockedSeats.stream()
                .allMatch(seat -> seat.getScreen().getId().equals(expectedScreenId));
        if (!allSeatsOnScreen) {
            throw new InvalidBookingException("One or more seats do not belong to this show's screen");
        }

        // 4. Re-check availability — safe, because we hold the lock
        List<Long> alreadyBookedSeatIds = bookingSeatRepository.findBookedSeatIdsByShowId(request.getShowId())
                .stream()
                .filter(sortedSeatIds::contains)
                .toList();

        if (!alreadyBookedSeatIds.isEmpty()) {
            throw new SeatAlreadyBookedException(
                    "Seat(s) " + alreadyBookedSeatIds + " are no longer available for this show");
        }

        // 5. Calculate total amount server-side
        BigDecimal totalAmount = show.getTicketPrice().multiply(BigDecimal.valueOf(lockedSeats.size()));

        // 6. Create the booking in PENDING state
        Booking booking = new Booking();
        booking.setBookingReference("BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setUser(user);
        booking.setShow(show);
        booking.setTotalAmount(totalAmount);
        booking.setBookingStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);

        // 7. Create BookingSeat rows (unique constraint on show_id+seat_id is the backstop)
        for (Seat seat : lockedSeats) {
            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(savedBooking);
            bookingSeat.setSeat(seat);
            bookingSeat.setShowId(show.getId());
            bookingSeat.setPrice(show.getTicketPrice());
            bookingSeatRepository.save(bookingSeat);
        }

        // 8. Process simulated payment
        Payment payment = paymentService.processPayment(savedBooking.getId(), totalAmount, request.getPaymentMethod());

        if (payment == null) {
            // Throwing rolls back the WHOLE transaction: booking + booking_seats
            // undone, locks released — seat is freed, matching the spec's flow.
            throw new PaymentFailedException("Payment failed for booking attempt on show " + show.getId());
        }

        payment.setBooking(savedBooking);
        paymentRepository.save(payment);

        // 9. Confirm booking
        savedBooking.setBookingStatus(BookingStatus.CONFIRMED);
        Booking confirmed = bookingRepository.save(savedBooking);

        return bookingMapper.toResponse(confirmed, payment.getPaymentStatus().name());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));

        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAdmin && !booking.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("You do not have access to this booking");
        }

        String paymentStatus = paymentRepository.findByBookingId(booking.getId())
                .map(p -> p.getPaymentStatus().name())
                .orElse(null);

        return bookingMapper.toResponse(booking, paymentStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        return bookingRepository.findByUserId(user.getId()).stream()
                .map(booking -> {
                    String paymentStatus = paymentRepository.findByBookingId(booking.getId())
                            .map(p -> p.getPaymentStatus().name())
                            .orElse(null);
                    return bookingMapper.toResponse(booking, paymentStatus);
                })
                .toList();
    }

    @Override
    @Transactional
    public void cancelBooking(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("You can only cancel your own bookings");
        }

        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingException("Only CONFIRMED bookings can be cancelled");
        }

        Show show = booking.getShow();
        LocalDateTime showDateTime = LocalDateTime.of(show.getShowDate(), show.getStartTime());
        if (showDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidBookingException("Cannot cancel a booking for a show that has already started");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}
