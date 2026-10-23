package com.ticketbook.controller;

import com.ticketbook.dto.SeatRequest;
import com.ticketbook.dto.SeatResponse;
import com.ticketbook.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/api/screens/{screenId}/seats")
    public ResponseEntity<List<SeatResponse>> getByScreen(@PathVariable Long screenId) {
        return ResponseEntity.ok(seatService.getByScreen(screenId));
    }

    @PostMapping("/api/admin/screens/{screenId}/seats")
    public ResponseEntity<SeatResponse> create(@PathVariable Long screenId, @Valid @RequestBody SeatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seatService.create(screenId, request));
    }
}
