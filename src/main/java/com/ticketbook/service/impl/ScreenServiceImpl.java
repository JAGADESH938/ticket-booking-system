package com.ticketbook.service.impl;

import com.ticketbook.dto.ScreenRequest;
import com.ticketbook.dto.ScreenResponse;
import com.ticketbook.entity.Screen;
import com.ticketbook.entity.Theatre;
import com.ticketbook.exception.ScreenNotFoundException;
import com.ticketbook.exception.TheatreNotFoundException;
import com.ticketbook.mapper.ScreenMapper;
import com.ticketbook.repository.ScreenRepository;
import com.ticketbook.repository.TheatreRepository;
import com.ticketbook.service.ScreenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenServiceImpl implements ScreenService {

    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenMapper screenMapper;

    @Override
    @Transactional
    public ScreenResponse create(Long theatreId, ScreenRequest request) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new TheatreNotFoundException("Theatre not found with id: " + theatreId));

        Screen screen = new Screen();
        screen.setScreenName(request.getScreenName());
        screen.setCapacity(request.getCapacity());
        screen.setTheatre(theatre);

        Screen saved = screenRepository.save(screen);
        return screenMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ScreenResponse update(Long id, ScreenRequest request) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ScreenNotFoundException("Screen not found with id: " + id));
        screen.setScreenName(request.getScreenName());
        screen.setCapacity(request.getCapacity());
        return screenMapper.toResponse(screenRepository.save(screen));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ScreenNotFoundException("Screen not found with id: " + id));
        screenRepository.delete(screen);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScreenResponse> getByTheatre(Long theatreId) {
        if (!theatreRepository.existsById(theatreId)) {
            throw new TheatreNotFoundException("Theatre not found with id: " + theatreId);
        }
        return screenRepository.findByTheatreId(theatreId).stream()
                .map(screenMapper::toResponse)
                .toList();
    }
}
