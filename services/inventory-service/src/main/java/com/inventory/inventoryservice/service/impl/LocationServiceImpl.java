package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.model.Location;
import com.inventory.inventoryservice.repository.LocationRepository;
import com.inventory.inventoryservice.service.LocationService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LocationServiceImpl implements LocationService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);
    
    private final LocationRepository locationRepository;
    
    @Autowired
    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }
    
    @Override
    @Cacheable(value = "locations")
    public List<Location> getAllLocations() {
        logger.info("Fetching all locations");
        return locationRepository.findAll();
    }
    
    @Override
    @Cacheable(value = "locations", key = "#id")
    public Optional<Location> getLocationById(Long id) {
        logger.info("Fetching location with id: {}", id);
        return locationRepository.findById(id);
    }
    
    @Override
    @Cacheable(value = "locations", key = "#name")
    public Optional<Location> getLocationByName(String name) {
        logger.info("Fetching location with name: {}", name);
        return locationRepository.findByName(name);
    }
    
    @Override
    @Cacheable(value = "locationsByCity", key = "#city")
    public List<Location> getLocationsByCity(String city) {
        logger.info("Fetching locations in city: {}", city);
        return locationRepository.findByCity(city);
    }
    
    @Override
    @Cacheable(value = "locationsByCountry", key = "#country")
    public List<Location> getLocationsByCountry(String country) {
        logger.info("Fetching locations in country: {}", country);
        return locationRepository.findByCountry(country);
    }
    
    @Override
    @Transactional
    public Location createLocation(Location location) {
        logger.info("Creating new location: {}", location.getName());
        if (locationRepository.existsByName(location.getName())) {
            throw new IllegalArgumentException("Location with name " + location.getName() + " already exists");
        }
        return locationRepository.save(location);
    }
    
    @Override
    @Transactional
    @CachePut(value = "locations", key = "#id")
    public Location updateLocation(Long id, Location location) {
        logger.info("Updating location with id: {}", id);
        Location existingLocation = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found with id: " + id));
        
        // Check if the new name already exists for another location
        if (!existingLocation.getName().equals(location.getName()) && 
                locationRepository.existsByName(location.getName())) {
            throw new IllegalArgumentException("Location with name " + location.getName() + " already exists");
        }
        
        existingLocation.setName(location.getName());
        existingLocation.setAddress(location.getAddress());
        existingLocation.setCity(location.getCity());
        existingLocation.setState(location.getState());
        existingLocation.setZipCode(location.getZipCode());
        existingLocation.setCountry(location.getCountry());
        
        return locationRepository.save(existingLocation);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "locations", key = "#id")
    public void deleteLocation(Long id) {
        logger.info("Deleting location with id: {}", id);
        if (!locationRepository.existsById(id)) {
            throw new EntityNotFoundException("Location not found with id: " + id);
        }
        locationRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByName(String name) {
        return locationRepository.existsByName(name);
    }
}