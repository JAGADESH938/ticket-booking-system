package com.ticketbook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TheatreResponse {
    private Long id;
    private String name;
    private String location;
    private String city;
    private String status;
}
