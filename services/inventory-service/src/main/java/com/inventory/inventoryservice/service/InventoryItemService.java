package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.model.Category;
import com.inventory.inventoryservice.model.InventoryItem;
import com.inventory.inventoryservice.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InventoryItemService {
    
    List<InventoryItem> getAllItems();
    
    Page<InventoryItem> getAllItemsPaged(Pageable pageable);
    
    Optional<InventoryItem> getItemById(Long id);
    
    Optional<InventoryItem> getItemBySku(String sku);
    
    List<InventoryItem> getItemsByCategory(Category category);
    
    List<InventoryItem> getItemsByLocation(Location location);
    
    Page<InventoryItem> searchItems(String keyword, Pageable pageable);
    
    InventoryItem createItem(InventoryItem item);
    
    InventoryItem updateItem(Long id, InventoryItem item);
    
    void deleteItem(Long id);
    
    boolean existsBySku(String sku);
    
    /**
     * Update the quantity of an inventory item
     * @param id The ID of the inventory item
     * @param quantityChange The change in quantity (positive for increment, negative for decrement)
     * @return The updated inventory item
     */
    InventoryItem updateQuantity(Long id, int quantityChange);
    
    /**
     * Update the quantity of an inventory item by SKU
     * @param sku The SKU of the inventory item
     * @param quantityChange The change in quantity (positive for increment, negative for decrement)
     * @return The updated inventory item
     */
    InventoryItem updateQuantityBySku(String sku, int quantityChange);
    
    /**
     * Find all items that are below their threshold (low stock)
     * @return List of low stock items
     */
    List<InventoryItem> findLowStockItems();
    
    /**
     * Find all items at a specific location that are below their threshold (low stock)
     * @param locationId The ID of the location
     * @return List of low stock items at the specified location
     */
    List<InventoryItem> findLowStockItemsByLocation(Long locationId);
    
    /**
     * Find all items in a specific category that are below their threshold (low stock)
     * @param categoryId The ID of the category
     * @return List of low stock items in the specified category
     */
    List<InventoryItem> findLowStockItemsByCategory(Long categoryId);
    
    /**
     * Reserve inventory for an order
     * @param sku The SKU of the inventory item
     * @param quantity The quantity to reserve
     * @return True if reservation was successful, false otherwise
     */
    boolean reserveInventory(String sku, int quantity);
}