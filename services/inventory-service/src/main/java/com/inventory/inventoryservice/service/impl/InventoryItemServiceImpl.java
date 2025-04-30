package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.event.InventoryChangeEvent;
import com.inventory.inventoryservice.event.KafkaProducerService;
import com.inventory.inventoryservice.event.LowStockAlertEvent;
import com.inventory.inventoryservice.model.Category;
import com.inventory.inventoryservice.model.InventoryItem;
import com.inventory.inventoryservice.model.Location;
import com.inventory.inventoryservice.repository.InventoryItemRepository;
import com.inventory.inventoryservice.service.InventoryItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryItemServiceImpl implements InventoryItemService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryItemServiceImpl.class);

    private final InventoryItemRepository inventoryItemRepository;
    private final KafkaProducerService kafkaProducerService;

    public InventoryItemServiceImpl(InventoryItemRepository inventoryItemRepository, 
                                   KafkaProducerService kafkaProducerService) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    @Cacheable(value = "inventoryItems")
    public List<InventoryItem> getAllItems() {
        logger.info("Fetching all inventory items");
        return inventoryItemRepository.findAll();
    }

    @Override
    public Page<InventoryItem> getAllItemsPaged(Pageable pageable) {
        logger.info("Fetching inventory items with pagination");
        return inventoryItemRepository.findAll(pageable);
    }

    @Override
    @Cacheable(value = "inventoryItems", key = "#id")
    public Optional<InventoryItem> getItemById(Long id) {
        logger.info("Fetching inventory item with id: {}", id);
        return inventoryItemRepository.findById(id);
    }

    @Override
    @Cacheable(value = "inventoryItems", key = "#sku")
    public Optional<InventoryItem> getItemBySku(String sku) {
        logger.info("Fetching inventory item with SKU: {}", sku);
        return inventoryItemRepository.findBySku(sku);
    }

    @Override
    @Cacheable(value = "inventoryItemsByCategory", key = "#category.id")
    public List<InventoryItem> getItemsByCategory(Category category) {
        logger.info("Fetching inventory items by category: {}", category.getName());
        return inventoryItemRepository.findByCategory(category);
    }

    @Override
    @Cacheable(value = "inventoryItemsByLocation", key = "#location.id")
    public List<InventoryItem> getItemsByLocation(Location location) {
        logger.info("Fetching inventory items by location: {}", location.getName());
        return inventoryItemRepository.findByLocation(location);
    }

    @Override
    public Page<InventoryItem> searchItems(String keyword, Pageable pageable) {
        logger.info("Searching inventory items with keyword: {}", keyword);
        return inventoryItemRepository.findByNameContaining(keyword, pageable);
    }

    @Override
    @Transactional
    public InventoryItem createItem(InventoryItem item) {
        logger.info("Creating new inventory item with SKU: {}", item.getSku());
        if (inventoryItemRepository.existsBySku(item.getSku())) {
            throw new IllegalArgumentException("Inventory item with SKU " + item.getSku() + " already exists");
        }
        return inventoryItemRepository.save(item);
    }

    @Override
    @Transactional
    @CachePut(value = "inventoryItems", key = "#id")
    public InventoryItem updateItem(Long id, InventoryItem item) {
        logger.info("Updating inventory item with id: {}", id);
        InventoryItem existingItem = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id));

        // Check if the new SKU already exists for another item
        if (!existingItem.getSku().equals(item.getSku()) && 
                inventoryItemRepository.existsBySku(item.getSku())) {
            throw new IllegalArgumentException("Inventory item with SKU " + item.getSku() + " already exists");
        }

        existingItem.setName(item.getName());
        existingItem.setDescription(item.getDescription());
        existingItem.setSku(item.getSku());
        existingItem.setQuantity(item.getQuantity());
        existingItem.setThreshold(item.getThreshold());
        existingItem.setPrice(item.getPrice());
        existingItem.setCategory(item.getCategory());
        existingItem.setLocation(item.getLocation());

        return inventoryItemRepository.save(existingItem);
    }

    @Override
    @Transactional
    @CacheEvict(value = "inventoryItems", key = "#id")
    public void deleteItem(Long id) {
        logger.info("Deleting inventory item with id: {}", id);
        if (!inventoryItemRepository.existsById(id)) {
            throw new EntityNotFoundException("Inventory item not found with id: " + id);
        }
        inventoryItemRepository.deleteById(id);
    }

    @Override
    public boolean existsBySku(String sku) {
        return inventoryItemRepository.existsBySku(sku);
    }

    @Override
    @Transactional
    @CachePut(value = "inventoryItems", key = "#id")
    public InventoryItem updateQuantity(Long id, int quantityChange) {
        logger.info("Updating quantity for inventory item with id: {}, change: {}", id, quantityChange);
        InventoryItem item = inventoryItemRepository.findByIdWithLock(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id));

        int newQuantity = item.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Cannot reduce quantity below zero");
        }

        item.setQuantity(newQuantity);
        item.setUpdatedAt(LocalDateTime.now());

        InventoryItem updatedItem = inventoryItemRepository.save(item);

        // Check if the item is now low on stock
        if (updatedItem.isLowStock()) {
            logger.warn("Inventory item {} is low on stock. Current quantity: {}, Threshold: {}", 
                    updatedItem.getSku(), updatedItem.getQuantity(), updatedItem.getThreshold());

            // Publish inventory change event
            InventoryChangeEvent changeEvent = createInventoryChangeEvent(updatedItem, item.getQuantity(), "UPDATE");
            kafkaProducerService.publishInventoryChangeEvent(changeEvent);

            // Publish low stock alert event
            LowStockAlertEvent alertEvent = createLowStockAlertEvent(updatedItem, "NEW", "UPDATE");
            kafkaProducerService.publishLowStockAlertEvent(alertEvent);
        } else {
            // Publish inventory change event only
            InventoryChangeEvent changeEvent = createInventoryChangeEvent(updatedItem, item.getQuantity(), "UPDATE");
            kafkaProducerService.publishInventoryChangeEvent(changeEvent);
        }

        return updatedItem;
    }

    @Override
    @Transactional
    public InventoryItem updateQuantityBySku(String sku, int quantityChange) {
        logger.info("Updating quantity for inventory item with SKU: {}, change: {}", sku, quantityChange);
        InventoryItem item = inventoryItemRepository.findBySkuWithLock(sku)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with SKU: " + sku));

        return updateQuantity(item.getId(), quantityChange);
    }

    @Override
    @Cacheable(value = "lowStockItems")
    public List<InventoryItem> findLowStockItems() {
        logger.info("Finding all low stock items");
        return inventoryItemRepository.findLowStockItems();
    }

    @Override
    @Cacheable(value = "lowStockItemsByLocation", key = "#locationId")
    public List<InventoryItem> findLowStockItemsByLocation(Long locationId) {
        logger.info("Finding low stock items by location id: {}", locationId);
        return inventoryItemRepository.findLowStockItemsByLocation(locationId);
    }

    @Override
    @Cacheable(value = "lowStockItemsByCategory", key = "#categoryId")
    public List<InventoryItem> findLowStockItemsByCategory(Long categoryId) {
        logger.info("Finding low stock items by category id: {}", categoryId);
        return inventoryItemRepository.findLowStockItemsByCategory(categoryId);
    }

    @Override
    @Transactional
    public boolean reserveInventory(String sku, int quantity) {
        logger.info("Attempting to reserve {} units of item with SKU: {}", quantity, sku);
        try {
            InventoryItem item = inventoryItemRepository.findBySkuWithLock(sku)
                    .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with SKU: " + sku));

            if (item.getQuantity() >= quantity) {
                item.setQuantity(item.getQuantity() - quantity);
                item.setUpdatedAt(LocalDateTime.now());
                inventoryItemRepository.save(item);

                // Check if the item is now low on stock
                if (item.isLowStock()) {
                    logger.warn("Inventory item {} is low on stock after reservation. Current quantity: {}, Threshold: {}", 
                            item.getSku(), item.getQuantity(), item.getThreshold());

                    // Publish inventory change event
                    InventoryChangeEvent changeEvent = createInventoryChangeEvent(item, item.getQuantity() + quantity, "RESERVATION");
                    kafkaProducerService.publishInventoryChangeEvent(changeEvent);

                    // Publish low stock alert event
                    LowStockAlertEvent alertEvent = createLowStockAlertEvent(item, "NEW", "RESERVATION");
                    kafkaProducerService.publishLowStockAlertEvent(alertEvent);
                } else {
                    // Publish inventory change event only
                    InventoryChangeEvent changeEvent = createInventoryChangeEvent(item, item.getQuantity() + quantity, "RESERVATION");
                    kafkaProducerService.publishInventoryChangeEvent(changeEvent);
                }

                return true;
            } else {
                logger.warn("Cannot reserve {} units of item with SKU: {}. Available quantity: {}", 
                        quantity, sku, item.getQuantity());
                return false;
            }
        } catch (EntityNotFoundException e) {
            logger.error("Failed to reserve inventory: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Creates an InventoryChangeEvent from an InventoryItem.
     *
     * @param item The inventory item
     * @param oldQuantity The old quantity
     * @param changeType The type of change (INCREMENT, DECREMENT, RESERVATION)
     * @return The created InventoryChangeEvent
     */
    private InventoryChangeEvent createInventoryChangeEvent(InventoryItem item, int oldQuantity, String changeType) {
        return new InventoryChangeEvent(
                item.getId(),
                item.getSku(),
                item.getName(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getLocation().getId(),
                item.getLocation().getName(),
                oldQuantity,
                item.getQuantity(),
                item.getThreshold(),
                changeType
        );
    }

    /**
     * Creates a LowStockAlertEvent from an InventoryItem.
     *
     * @param item The inventory item
     * @param alertType The type of alert (NEW, CONTINUED)
     * @param triggerAction The action that triggered the alert (UPDATE, RESERVATION)
     * @return The created LowStockAlertEvent
     */
    private LowStockAlertEvent createLowStockAlertEvent(InventoryItem item, String alertType, String triggerAction) {
        return new LowStockAlertEvent(
                item.getId(),
                item.getSku(),
                item.getName(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getLocation().getId(),
                item.getLocation().getName(),
                item.getQuantity(),
                item.getThreshold(),
                alertType,
                triggerAction
        );
    }
}
