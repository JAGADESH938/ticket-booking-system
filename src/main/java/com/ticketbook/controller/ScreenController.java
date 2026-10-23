package com.ticketbook.controller;

import com.ticketbook.dto.ScreenRequest;
import com.ticketbook.dto.ScreenResponse;
import com.ticketbook.service.ScreenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;

    @GetMapping("/api/theatres/{theatreId}/screens")
    public ResponseEntity<List<ScreenResponse>> getByTheatre(@PathVariable Long theatreId) {
        return ResponseEntity.ok(screenService.getByTheatre(theatreId));
    }

    @PostMapping("/api/admin/theatres/{theatreId}/screens")
    public ResponseEntity<ScreenResponse> create(@PathVariable Long theatreId, @Valid @RequestBody ScreenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(screenService.create(theatreId, request));
    }

    @PutMapping("/api/admin/screens/{id}")
    public ResponseEntity<ScreenResponse> update(@PathVariable Long id, @Valid @RequestBody ScreenRequest request) {
        return ResponseEntity.ok(screenService.update(id, request));
    }

    @DeleteMapping("/api/admin/screens/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        screenService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
