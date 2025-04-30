package com.inventory.inventoryservice.event;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event that is published when an inventory item's quantity changes.
 */
public class InventoryChangeEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private String sku;
    private String itemName;
    private Long categoryId;
    private String categoryName;
    private Long locationId;
    private String locationName;
    private Integer oldQuantity;
    private Integer newQuantity;
    private Integer threshold;
    private LocalDateTime timestamp;
    private String changeType; // "INCREMENT", "DECREMENT", "RESERVATION"

    // Default constructor for serialization
    public InventoryChangeEvent() {
    }

    public InventoryChangeEvent(Long itemId, String sku, String itemName, Long categoryId, String categoryName,
                               Long locationId, String locationName, Integer oldQuantity, Integer newQuantity,
                               Integer threshold, String changeType) {
        this.itemId = itemId;
        this.sku = sku;
        this.itemName = itemName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.locationId = locationId;
        this.locationName = locationName;
        this.oldQuantity = oldQuantity;
        this.newQuantity = newQuantity;
        this.threshold = threshold;
        this.timestamp = LocalDateTime.now();
        this.changeType = changeType;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Integer getOldQuantity() {
        return oldQuantity;
    }

    public void setOldQuantity(Integer oldQuantity) {
        this.oldQuantity = oldQuantity;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public boolean isLowStock() {
        return threshold != null && newQuantity <= threshold;
    }

    @Override
    public String toString() {
        return "InventoryChangeEvent{" +
                "itemId=" + itemId +
                ", sku='" + sku + '\'' +
                ", itemName='" + itemName + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", locationId=" + locationId +
                ", locationName='" + locationName + '\'' +
                ", oldQuantity=" + oldQuantity +
                ", newQuantity=" + newQuantity +
                ", threshold=" + threshold +
                ", timestamp=" + timestamp +
                ", changeType='" + changeType + '\'' +
                '}';
    }
}