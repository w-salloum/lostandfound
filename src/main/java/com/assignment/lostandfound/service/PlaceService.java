package com.assignment.lostandfound.service;

import com.assignment.lostandfound.entity.Place;
import com.assignment.lostandfound.repository.PlaceRepository;
import org.springframework.stereotype.Service;

@Service
public class PlaceService {
    private final PlaceRepository placeRepository;

    public PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public Place getOrSaveLocation(String name) {
        return placeRepository.findByName(name).orElseGet(() -> placeRepository.save(new Place(name)));
    }
}
