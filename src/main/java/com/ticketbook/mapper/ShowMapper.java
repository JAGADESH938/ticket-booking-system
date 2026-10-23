package com.ticketbook.mapper;

import com.ticketbook.dto.ShowResponse;
import com.ticketbook.entity.Show;
import org.springframework.stereotype.Component;

@Component
public class ShowMapper {
    public ShowResponse toResponse(Show show) {
        return new ShowResponse(
                show.getId(),
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                show.getScreen().getId(),
                show.getScreen().getScreenName(),
                show.getScreen().getTheatre().getId(),
                show.getScreen().getTheatre().getName(),
                show.getShowDate(),
                show.getStartTime(),
                show.getEndTime(),
                show.getTicketPrice(),
                show.getStatus()
        );
    }
}
