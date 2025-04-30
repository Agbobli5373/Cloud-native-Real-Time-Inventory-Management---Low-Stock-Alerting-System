package com.inventory.inventoryservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

/**
 * Service for publishing events to Kafka topics.
 */
@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.inventory-changes}")
    private String inventoryChangesTopic;

    @Value("${app.kafka.topics.low-stock-alerts}")
    private String lowStockAlertsTopic;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes an inventory change event to the inventory-changes topic.
     *
     * @param event The inventory change event to publish
     */
    public void publishInventoryChangeEvent(InventoryChangeEvent event) {
        String key = event.getSku();
        publishEvent(inventoryChangesTopic, key, event);
    }

    /**
     * Publishes a low stock alert event to the low-stock-alerts topic.
     *
     * @param event The low stock alert event to publish
     */
    public void publishLowStockAlertEvent(LowStockAlertEvent event) {
        String key = event.getSku();
        publishEvent(lowStockAlertsTopic, key, event);
    }

    /**
     * Generic method to publish an event to a Kafka topic.
     *
     * @param topic The topic to publish to
     * @param key The key for the message
     * @param event The event to publish
     */
    private void publishEvent(String topic, String key, Object event) {
        try {
            kafkaTemplate.send(topic, key, event).thenAccept(result -> {
                logger.info("Event published to topic {} with key {}: {}", topic, key, event);
            }).exceptionally(ex -> {
                logger.error("Failed to publish event to topic {} with key {}: {}", topic, key, ex.getMessage(), ex);
                return null;
            });
        } catch (Exception e) {
            logger.error("Error publishing event to topic {} with key {}: {}", topic, key, e.getMessage(), e);
        }
    }
}