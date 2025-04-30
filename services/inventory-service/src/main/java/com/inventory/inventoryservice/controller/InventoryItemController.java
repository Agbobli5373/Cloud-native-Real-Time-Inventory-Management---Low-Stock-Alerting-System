package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.model.Category;
import com.inventory.inventoryservice.model.InventoryItem;
import com.inventory.inventoryservice.model.Location;
import com.inventory.inventoryservice.service.CategoryService;
import com.inventory.inventoryservice.service.InventoryItemService;
import com.inventory.inventoryservice.service.LocationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InventoryItemController {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryItemController.class);
    
    private final InventoryItemService inventoryItemService;
    private final CategoryService categoryService;
    private final LocationService locationService;
    

    public InventoryItemController(InventoryItemService inventoryItemService, 
                                  CategoryService categoryService,
                                  LocationService locationService) {
        this.inventoryItemService = inventoryItemService;
        this.categoryService = categoryService;
        this.locationService = locationService;
    }
    
    @GetMapping
    public ResponseEntity<List<InventoryItem>> getAllItems() {
        logger.info("REST request to get all Inventory Items");
        List<InventoryItem> items = inventoryItemService.getAllItems();
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<InventoryItem>> getAllItemsPaged(Pageable pageable) {
        logger.info("REST request to get paged Inventory Items");
        Page<InventoryItem> page = inventoryItemService.getAllItemsPaged(pageable);
        return ResponseEntity.ok(page);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getItemById(@PathVariable Long id) {
        logger.info("REST request to get Inventory Item : {}", id);
        return inventoryItemService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/sku/{sku}")
    public ResponseEntity<InventoryItem> getItemBySku(@PathVariable String sku) {
        logger.info("REST request to get Inventory Item by SKU : {}", sku);
        return inventoryItemService.getItemBySku(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<InventoryItem>> getItemsByCategory(@PathVariable Long categoryId) {
        logger.info("REST request to get Inventory Items by Category ID : {}", categoryId);
        return categoryService.getCategoryById(categoryId)
                .map(category -> ResponseEntity.ok(inventoryItemService.getItemsByCategory(category)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<InventoryItem>> getItemsByLocation(@PathVariable Long locationId) {
        logger.info("REST request to get Inventory Items by Location ID : {}", locationId);
        return locationService.getLocationById(locationId)
                .map(location -> ResponseEntity.ok(inventoryItemService.getItemsByLocation(location)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<InventoryItem>> searchItems(@RequestParam String keyword, Pageable pageable) {
        logger.info("REST request to search Inventory Items with keyword : {}", keyword);
        Page<InventoryItem> page = inventoryItemService.searchItems(keyword, pageable);
        return ResponseEntity.ok(page);
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItem>> getLowStockItems() {
        logger.info("REST request to get low stock Inventory Items");
        List<InventoryItem> items = inventoryItemService.findLowStockItems();
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/low-stock/category/{categoryId}")
    public ResponseEntity<List<InventoryItem>> getLowStockItemsByCategory(@PathVariable Long categoryId) {
        logger.info("REST request to get low stock Inventory Items by Category ID : {}", categoryId);
        List<InventoryItem> items = inventoryItemService.findLowStockItemsByCategory(categoryId);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/low-stock/location/{locationId}")
    public ResponseEntity<List<InventoryItem>> getLowStockItemsByLocation(@PathVariable Long locationId) {
        logger.info("REST request to get low stock Inventory Items by Location ID : {}", locationId);
        List<InventoryItem> items = inventoryItemService.findLowStockItemsByLocation(locationId);
        return ResponseEntity.ok(items);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<InventoryItem> createItem(@Valid @RequestBody InventoryItem item) {
        logger.info("REST request to save Inventory Item : {}", item);
        if (item.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new inventory item cannot already have an ID").build();
        }
        if (inventoryItemService.existsBySku(item.getSku())) {
            return ResponseEntity.badRequest().header("Failure", "Inventory item with SKU " + item.getSku() + " already exists").build();
        }
        
        try {
            // Validate category and location exist
            Category category = categoryService.getCategoryById(item.getCategory().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + item.getCategory().getId()));
            Location location = locationService.getLocationById(item.getLocation().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Location not found with id: " + item.getLocation().getId()));
            
            item.setCategory(category);
            item.setLocation(location);
            
            InventoryItem result = inventoryItemService.createItem(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().header("Failure", e.getMessage()).build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<InventoryItem> updateItem(@PathVariable Long id, @Valid @RequestBody InventoryItem item) {
        logger.info("REST request to update Inventory Item : {}, {}", id, item);
        if (item.getId() == null) {
            return ResponseEntity.badRequest().header("Failure", "ID cannot be null for update").build();
        }
        if (!id.equals(item.getId())) {
            return ResponseEntity.badRequest().header("Failure", "ID in path and body do not match").build();
        }
        
        try {
            // Validate category and location exist
            Category category = categoryService.getCategoryById(item.getCategory().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + item.getCategory().getId()));
            Location location = locationService.getLocationById(item.getLocation().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Location not found with id: " + item.getLocation().getId()));
            
            item.setCategory(category);
            item.setLocation(location);
            
            InventoryItem result = inventoryItemService.updateItem(id, item);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().header("Failure", e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().header("Failure", e.getMessage()).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        logger.info("REST request to delete Inventory Item : {}", id);
        try {
            inventoryItemService.deleteItem(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/quantity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<InventoryItem> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        Integer quantityChange = payload.get("quantityChange");
        if (quantityChange == null) {
            return ResponseEntity.badRequest().header("Failure", "quantityChange is required").build();
        }
        
        logger.info("REST request to update quantity of Inventory Item : {}, change: {}", id, quantityChange);
        try {
            InventoryItem result = inventoryItemService.updateQuantity(id, quantityChange);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().header("Failure", e.getMessage()).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/sku/{sku}/quantity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<InventoryItem> updateQuantityBySku(@PathVariable String sku, @RequestBody Map<String, Integer> payload) {
        Integer quantityChange = payload.get("quantityChange");
        if (quantityChange == null) {
            return ResponseEntity.badRequest().header("Failure", "quantityChange is required").build();
        }
        
        logger.info("REST request to update quantity of Inventory Item with SKU : {}, change: {}", sku, quantityChange);
        try {
            InventoryItem result = inventoryItemService.updateQuantityBySku(sku, quantityChange);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().header("Failure", e.getMessage()).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/reserve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER') or hasRole('USER')")
    public ResponseEntity<Map<String, Boolean>> reserveInventory(@RequestBody Map<String, Object> payload) {
        String sku = (String) payload.get("sku");
        Integer quantity = (Integer) payload.get("quantity");
        
        if (sku == null || quantity == null) {
            return ResponseEntity.badRequest().header("Failure", "sku and quantity are required").build();
        }
        
        logger.info("REST request to reserve inventory for SKU : {}, quantity: {}", sku, quantity);
        boolean reserved = inventoryItemService.reserveInventory(sku, quantity);
        return ResponseEntity.ok(Map.of("reserved", reserved));
    }
}