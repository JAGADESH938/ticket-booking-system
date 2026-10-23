package com.ticketbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScreenRequest {
    @NotBlank
    private String screenName;

    @Positive
    private Integer capacity;
}
