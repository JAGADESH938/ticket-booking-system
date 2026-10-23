package com.ticketbook.dto;

import com.ticketbook.entity.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BookingRequest {
    @NotNull
    private Long showId;

    @NotEmpty(message = "At least one seat must be selected")
    @Size(max = 10, message = "Cannot book more than 10 seats in a single request")
    private List<Long> seatIds;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
