package com.inventory.inventoryservice.config;

import com.inventory.inventoryservice.model.Category;
import com.inventory.inventoryservice.model.InventoryItem;
import com.inventory.inventoryservice.model.Location;
import com.inventory.inventoryservice.repository.CategoryRepository;
import com.inventory.inventoryservice.repository.InventoryItemRepository;
import com.inventory.inventoryservice.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("!prod")
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            logger.info("Initializing sample data...");

            // Create categories if they don't exist
            if (categoryRepository.count() == 0) {
                List<Category> categories = Arrays.asList(
                        new Category("Electronics", "Electronic devices and accessories"),
                        new Category("Clothing", "Apparel and fashion items"),
                        new Category("Books", "Books and publications"),
                        new Category("Furniture", "Home and office furniture"),
                        new Category("Groceries", "Food and household items")
                );
                categoryRepository.saveAll(categories);
                logger.info("Sample categories created");
            }

            // Create locations if they don't exist
            if (locationRepository.count() == 0) {
                List<Location> locations = Arrays.asList(
                        new Location("Main Warehouse", "123 Main St", "New York", "NY", "10001", "USA"),
                        new Location("West Coast Warehouse", "456 Ocean Ave", "Los Angeles", "CA", "90001", "USA"),
                        new Location("East Coast Warehouse", "789 Atlantic Blvd", "Boston", "MA", "02101", "USA"),
                        new Location("European Warehouse", "10 Euro Lane", "London", "Greater London", "EC1A 1BB", "UK"),
                        new Location("Asian Warehouse", "20 Asia Road", "Tokyo", "Tokyo", "100-0001", "Japan")
                );
                locationRepository.saveAll(locations);
                logger.info("Sample locations created");
            }

            // Create inventory items if they don't exist
            if (inventoryItemRepository.count() == 0) {
                List<Category> categories = categoryRepository.findAll();
                List<Location> locations = locationRepository.findAll();

                if (!categories.isEmpty() && !locations.isEmpty()) {
                    Category electronics = categories.stream()
                            .filter(c -> c.getName().equals("Electronics"))
                            .findFirst()
                            .orElse(categories.get(0));

                    Category clothing = categories.stream()
                            .filter(c -> c.getName().equals("Clothing"))
                            .findFirst()
                            .orElse(categories.get(0));

                    Location mainWarehouse = locations.stream()
                            .filter(l -> l.getName().equals("Main Warehouse"))
                            .findFirst()
                            .orElse(locations.get(0));

                    Location westCoastWarehouse = locations.stream()
                            .filter(l -> l.getName().equals("West Coast Warehouse"))
                            .findFirst()
                            .orElse(locations.get(0));

                    List<InventoryItem> items = Arrays.asList(
                            new InventoryItem("Laptop", "High-performance laptop", "ELEC-001", 100, 20, new BigDecimal("999.99"), electronics, mainWarehouse),
                            new InventoryItem("Smartphone", "Latest smartphone model", "ELEC-002", 200, 30, new BigDecimal("699.99"), electronics, mainWarehouse),
                            new InventoryItem("Tablet", "10-inch tablet", "ELEC-003", 50, 10, new BigDecimal("399.99"), electronics, westCoastWarehouse),
                            new InventoryItem("T-Shirt", "Cotton t-shirt", "CLOTH-001", 500, 100, new BigDecimal("19.99"), clothing, mainWarehouse),
                            new InventoryItem("Jeans", "Denim jeans", "CLOTH-002", 300, 50, new BigDecimal("49.99"), clothing, westCoastWarehouse),
                            new InventoryItem("Headphones", "Wireless headphones", "ELEC-004", 75, 15, new BigDecimal("149.99"), electronics, westCoastWarehouse),
                            new InventoryItem("Smartwatch", "Fitness tracking smartwatch", "ELEC-005", 30, 10, new BigDecimal("249.99"), electronics, mainWarehouse)
                    );
                    inventoryItemRepository.saveAll(items);
                    logger.info("Sample inventory items created");
                }
            }

            logger.info("Data initialization completed");
        };
    }
}