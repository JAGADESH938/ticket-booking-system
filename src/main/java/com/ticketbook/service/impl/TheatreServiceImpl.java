package com.ticketbook.service.impl;

import com.ticketbook.dto.TheatreRequest;
import com.ticketbook.dto.TheatreResponse;
import com.ticketbook.entity.Theatre;
import com.ticketbook.exception.TheatreNotFoundException;
import com.ticketbook.mapper.TheatreMapper;
import com.ticketbook.repository.TheatreRepository;
import com.ticketbook.service.TheatreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TheatreServiceImpl implements TheatreService {

    private final TheatreRepository theatreRepository;
    private final TheatreMapper theatreMapper;

    @Override
    @Transactional
    public TheatreResponse create(TheatreRequest request) {
        Theatre theatre = theatreMapper.toEntity(request);
        theatre.setStatus("ACTIVE");
        Theatre saved = theatreRepository.save(theatre);
        return theatreMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TheatreResponse update(Long id, TheatreRequest request) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new TheatreNotFoundException("Theatre not found with id: " + id));
        theatreMapper.updateEntity(theatre, request);
        return theatreMapper.toResponse(theatreRepository.save(theatre));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new TheatreNotFoundException("Theatre not found with id: " + id));
        theatre.setStatus("INACTIVE");
        theatreRepository.save(theatre);
    }

    @Override
    @Transactional(readOnly = true)
    public TheatreResponse getById(Long id) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new TheatreNotFoundException("Theatre not found with id: " + id));
        return theatreMapper.toResponse(theatre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TheatreResponse> getAll() {
        return theatreRepository.findAll().stream().map(theatreMapper::toResponse).toList();
    }
}
