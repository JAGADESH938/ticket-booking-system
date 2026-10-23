package com.ticketbook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScreenResponse {
    private Long id;
    private String screenName;
    private Integer capacity;
    private Long theatreId;
}
