package com.ticketbook.repository;

import com.ticketbook.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("""
        SELECT m FROM Movie m
        WHERE (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:genre IS NULL OR m.genre = :genre)
          AND (:language IS NULL OR m.language = :language)
          AND m.status = 'ACTIVE'
        ORDER BY m.releaseDate DESC
        """)
    Page<Movie> search(
            @Param("keyword") String keyword,
            @Param("genre") String genre,
            @Param("language") String language,
            Pageable pageable
    );
}
