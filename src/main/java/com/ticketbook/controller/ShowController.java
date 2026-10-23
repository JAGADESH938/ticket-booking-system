package com.ticketbook.controller;

import com.ticketbook.dto.SeatAvailabilityResponse;
import com.ticketbook.dto.ShowRequest;
import com.ticketbook.dto.ShowResponse;
import com.ticketbook.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @GetMapping("/api/shows")
    public ResponseEntity<Page<ShowResponse>> search(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theatreId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
    ) {
        return ResponseEntity.ok(showService.search(movieId, theatreId, date, pageable));
    }

    @GetMapping("/api/shows/{id}")
    public ResponseEntity<ShowResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(showService.getById(id));
    }

    @GetMapping("/api/shows/{showId}/seats")
    public ResponseEntity<List<SeatAvailabilityResponse>> getSeatAvailability(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getSeatAvailability(showId));
    }

    @PostMapping("/api/admin/shows")
    public ResponseEntity<ShowResponse> create(@Valid @RequestBody ShowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.create(request));
    }

    @PutMapping("/api/admin/shows/{id}")
    public ResponseEntity<ShowResponse> update(@PathVariable Long id, @Valid @RequestBody ShowRequest request) {
        return ResponseEntity.ok(showService.update(id, request));
    }

    @DeleteMapping("/api/admin/shows/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        showService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
