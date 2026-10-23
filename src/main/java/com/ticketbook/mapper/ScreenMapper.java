package com.ticketbook.mapper;

import com.ticketbook.dto.ScreenResponse;
import com.ticketbook.entity.Screen;
import org.springframework.stereotype.Component;

@Component
public class ScreenMapper {
    public ScreenResponse toResponse(Screen screen) {
        return new ScreenResponse(
                screen.getId(),
                screen.getScreenName(),
                screen.getCapacity(),
                screen.getTheatre().getId()
        );
    }
}
