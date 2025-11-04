package com.kyojin.tawsila.controller;

import com.kyojin.tawsila.dto.TourDTO;
import com.kyojin.tawsila.dto.TourDistanceDTO;
import com.kyojin.tawsila.entity.Tour;
import com.kyojin.tawsila.enums.AlgorithmType;
import com.kyojin.tawsila.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tours")
public class TourController {

    private final TourService tourService;

    @Autowired
    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @PostMapping
    public ResponseEntity<TourDTO> createTour(@Valid @RequestBody TourDTO tourDTO) {
        var createdTour = tourService.createTour(tourDTO);
        return ResponseEntity.ok(createdTour);
    }

    @GetMapping
    public ResponseEntity<List<TourDTO>> getAllTours() {
        return  ResponseEntity.ok(tourService.getAllTours());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourDTO> getTour(@PathVariable Long id) {
        return tourService.getTourById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourDTO> updateTour(@PathVariable Long id, @Valid @RequestBody TourDTO tourDTO) {
        var updatedTour = tourService.updateTour(id, tourDTO);
        return ResponseEntity.ok(updatedTour);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/optimize")
    public ResponseEntity<TourDTO> optimizeTour(@PathVariable Long id) {
        var optimizedTour = tourService.getOptimizedTour(id);
        return ResponseEntity.ok(optimizedTour);
    }

    @GetMapping("/{id}/distance")
    public ResponseEntity<TourDistanceDTO> getTourDistance(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.getTotalDistance(id));
    }
}
