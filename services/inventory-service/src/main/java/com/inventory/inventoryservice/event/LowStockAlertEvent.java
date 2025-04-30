package com.inventory.inventoryservice.event;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event that is published when an inventory item's quantity falls below its threshold.
 */
public class LowStockAlertEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private String sku;
    private String itemName;
    private Long categoryId;
    private String categoryName;
    private Long locationId;
    private String locationName;
    private Integer currentQuantity;
    private Integer threshold;
    private LocalDateTime timestamp;
    private String alertType; // "NEW", "CONTINUED"
    private String triggerAction; // "UPDATE", "RESERVATION"

    // Default constructor for serialization
    public LowStockAlertEvent() {
    }

    public LowStockAlertEvent(Long itemId, String sku, String itemName, Long categoryId, String categoryName,
                             Long locationId, String locationName, Integer currentQuantity, Integer threshold,
                             String alertType, String triggerAction) {
        this.itemId = itemId;
        this.sku = sku;
        this.itemName = itemName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.locationId = locationId;
        this.locationName = locationName;
        this.currentQuantity = currentQuantity;
        this.threshold = threshold;
        this.timestamp = LocalDateTime.now();
        this.alertType = alertType;
        this.triggerAction = triggerAction;
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

    public Integer getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(Integer currentQuantity) {
        this.currentQuantity = currentQuantity;
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

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getTriggerAction() {
        return triggerAction;
    }

    public void setTriggerAction(String triggerAction) {
        this.triggerAction = triggerAction;
    }

    public int getDeficitQuantity() {
        return threshold - currentQuantity;
    }

    public double getDeficitPercentage() {
        return threshold > 0 ? (double) (threshold - currentQuantity) / threshold * 100 : 0;
    }

    @Override
    public String toString() {
        return "LowStockAlertEvent{" +
                "itemId=" + itemId +
                ", sku='" + sku + '\'' +
                ", itemName='" + itemName + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", locationId=" + locationId +
                ", locationName='" + locationName + '\'' +
                ", currentQuantity=" + currentQuantity +
                ", threshold=" + threshold +
                ", timestamp=" + timestamp +
                ", alertType='" + alertType + '\'' +
                ", triggerAction='" + triggerAction + '\'' +
                '}';
    }
}