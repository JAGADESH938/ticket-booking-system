package com.ticketbook.repository;

import com.ticketbook.entity.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("""
        SELECT s FROM Show s
        WHERE (:movieId IS NULL OR s.movie.id = :movieId)
          AND (:theatreId IS NULL OR s.screen.theatre.id = :theatreId)
          AND (:date IS NULL OR s.showDate = :date)
          AND s.status = 'SCHEDULED'
        ORDER BY s.showDate, s.startTime
        """)
    Page<Show> search(
            @Param("movieId") Long movieId,
            @Param("theatreId") Long theatreId,
            @Param("date") LocalDate date,
            Pageable pageable
    );

    @Query("""
        SELECT s FROM Show s
        WHERE s.screen.id = :screenId
          AND s.showDate = :showDate
          AND s.status = 'SCHEDULED'
          AND s.startTime < :newEndTime
          AND :newStartTime < s.endTime
        """)
    List<Show> findOverlappingShows(
            @Param("screenId") Long screenId,
            @Param("showDate") LocalDate showDate,
            @Param("newStartTime") LocalTime newStartTime,
            @Param("newEndTime") LocalTime newEndTime
    );
}
