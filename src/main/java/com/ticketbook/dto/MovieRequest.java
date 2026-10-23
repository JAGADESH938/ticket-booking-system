package com.ticketbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MovieRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String genre;
    private String language;

    @Positive(message = "Duration must be a positive number of minutes")
    private Integer duration;

    private LocalDate releaseDate;
}
