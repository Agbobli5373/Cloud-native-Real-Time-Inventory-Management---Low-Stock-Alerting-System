package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.model.Location;
import com.inventory.inventoryservice.service.LocationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LocationController {
    
    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
    
    private final LocationService locationService;
    
    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }
    
    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        logger.info("REST request to get all Locations");
        List<Location> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        logger.info("REST request to get Location : {}", id);
        return locationService.getLocationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<Location> getLocationByName(@PathVariable String name) {
        logger.info("REST request to get Location by name : {}", name);
        return locationService.getLocationByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/city/{city}")
    public ResponseEntity<List<Location>> getLocationsByCity(@PathVariable String city) {
        logger.info("REST request to get Locations by city : {}", city);
        List<Location> locations = locationService.getLocationsByCity(city);
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping("/country/{country}")
    public ResponseEntity<List<Location>> getLocationsByCountry(@PathVariable String country) {
        logger.info("REST request to get Locations by country : {}", country);
        List<Location> locations = locationService.getLocationsByCountry(country);
        return ResponseEntity.ok(locations);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<Location> createLocation(@Valid @RequestBody Location location) {
        logger.info("REST request to save Location : {}", location);
        if (location.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new location cannot already have an ID").build();
        }
        if (locationService.existsByName(location.getName())) {
            return ResponseEntity.badRequest().header("Failure", "Location name already exists").build();
        }
        Location result = locationService.createLocation(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id, @Valid @RequestBody Location location) {
        logger.info("REST request to update Location : {}, {}", id, location);
        if (location.getId() == null) {
            return ResponseEntity.badRequest().header("Failure", "ID cannot be null for update").build();
        }
        if (!id.equals(location.getId())) {
            return ResponseEntity.badRequest().header("Failure", "ID in path and body do not match").build();
        }
        
        try {
            Location result = locationService.updateLocation(id, location);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().header("Failure", e.getMessage()).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        logger.info("REST request to delete Location : {}", id);
        try {
            locationService.deleteLocation(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}