package com.ticketbook.repository;

import com.ticketbook.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    @Query("""
        SELECT bs.seat.id FROM BookingSeat bs
        WHERE bs.booking.show.id = :showId
          AND bs.booking.bookingStatus IN ('PENDING', 'CONFIRMED')
        """)
    List<Long> findBookedSeatIdsByShowId(@Param("showId") Long showId);
}
