package com.ticketbook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TheatreRequest {
    @NotBlank
    private String name;
    private String location;
    private String city;
}
