package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.model.Location;

import java.util.List;
import java.util.Optional;

public interface LocationService {
    
    List<Location> getAllLocations();
    
    Optional<Location> getLocationById(Long id);
    
    Optional<Location> getLocationByName(String name);
    
    List<Location> getLocationsByCity(String city);
    
    List<Location> getLocationsByCountry(String country);
    
    Location createLocation(Location location);
    
    Location updateLocation(Long id, Location location);
    
    void deleteLocation(Long id);
    
    boolean existsByName(String name);
}