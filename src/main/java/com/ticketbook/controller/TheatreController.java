package com.ticketbook.controller;

import com.ticketbook.dto.TheatreRequest;
import com.ticketbook.dto.TheatreResponse;
import com.ticketbook.service.TheatreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TheatreController {

    private final TheatreService theatreService;

    @GetMapping("/api/theatres")
    public ResponseEntity<List<TheatreResponse>> getAll() {
        return ResponseEntity.ok(theatreService.getAll());
    }

    @GetMapping("/api/theatres/{id}")
    public ResponseEntity<TheatreResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(theatreService.getById(id));
    }

    @PostMapping("/api/admin/theatres")
    public ResponseEntity<TheatreResponse> create(@Valid @RequestBody TheatreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(theatreService.create(request));
    }

    @PutMapping("/api/admin/theatres/{id}")
    public ResponseEntity<TheatreResponse> update(@PathVariable Long id, @Valid @RequestBody TheatreRequest request) {
        return ResponseEntity.ok(theatreService.update(id, request));
    }

    @DeleteMapping("/api/admin/theatres/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        theatreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
