package com.ticketbook.mapper;

import com.ticketbook.dto.TheatreRequest;
import com.ticketbook.dto.TheatreResponse;
import com.ticketbook.entity.Theatre;
import org.springframework.stereotype.Component;

@Component
public class TheatreMapper {

    public Theatre toEntity(TheatreRequest request) {
        Theatre theatre = new Theatre();
        theatre.setName(request.getName());
        theatre.setLocation(request.getLocation());
        theatre.setCity(request.getCity());
        return theatre;
    }

    public void updateEntity(Theatre theatre, TheatreRequest request) {
        theatre.setName(request.getName());
        theatre.setLocation(request.getLocation());
        theatre.setCity(request.getCity());
    }

    public TheatreResponse toResponse(Theatre theatre) {
        return new TheatreResponse(
                theatre.getId(),
                theatre.getName(),
                theatre.getLocation(),
                theatre.getCity(),
                theatre.getStatus()
        );
    }
}
