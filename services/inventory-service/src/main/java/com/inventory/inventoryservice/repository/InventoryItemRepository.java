package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.model.Category;
import com.inventory.inventoryservice.model.InventoryItem;
import com.inventory.inventoryservice.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    
    Optional<InventoryItem> findBySku(String sku);
    
    boolean existsBySku(String sku);
    
    List<InventoryItem> findByCategory(Category category);
    
    List<InventoryItem> findByLocation(Location location);
    
    Page<InventoryItem> findByNameContaining(String name, Pageable pageable);
    
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.threshold")
    List<InventoryItem> findLowStockItems();
    
    @Query("SELECT i FROM InventoryItem i WHERE i.location.id = :locationId AND i.quantity <= i.threshold")
    List<InventoryItem> findLowStockItemsByLocation(@Param("locationId") Long locationId);
    
    @Query("SELECT i FROM InventoryItem i WHERE i.category.id = :categoryId AND i.quantity <= i.threshold")
    List<InventoryItem> findLowStockItemsByCategory(@Param("categoryId") Long categoryId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.id = :id")
    Optional<InventoryItem> findByIdWithLock(@Param("id") Long id);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.sku = :sku")
    Optional<InventoryItem> findBySkuWithLock(@Param("sku") String sku);
}