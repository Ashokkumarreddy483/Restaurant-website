package com.example.restaurant.controller; // <--- Ensure package is correct

import com.example.restaurant.model.CustomerOrder;
import com.example.restaurant.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController // <--- CRITICAL: This makes it a REST controller
@RequestMapping("/api/orders") // <--- CRITICAL: Base path for all order APIs
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<CustomerOrder>> getAllOrders() {
        logger.info("Received request to get all orders"); // Add logging
        List<CustomerOrder> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrder> getOrderById(@PathVariable Long id) {
        logger.info("Received request to get order by ID: {}", id); // Add logging
        return orderService.getOrderById(id)
                .map(order -> {
                    logger.info("Found order: {}", order.getId());
                    return ResponseEntity.ok(order);
                })
                .orElseGet(() -> {
                    logger.warn("Order not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CustomerOrder orderRequest) {
        logger.info("Received request to create order: {}", orderRequest); // Add logging
        try {
            // Ensure orderItems in the request are correctly structured
            // The frontend should send something like:
            // { "customerName": "John Doe", "orderItems": [ { "menuItem": {"id": 1}, "quantity": 2 }, ... ] }
            if (orderRequest.getOrderItems() == null || orderRequest.getOrderItems().isEmpty()) {
                logger.warn("Attempted to create an order with no items.");
                return ResponseEntity.badRequest().body("Order must contain at least one item.");
            }
            CustomerOrder createdOrder = orderService.createOrder(orderRequest);
            logger.info("Order created successfully with ID: {}", createdOrder.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (RuntimeException e) {
            logger.error("Error creating order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating order: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerOrder> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("status");
        logger.info("Received request to update status of order ID: {} to status: {}", id, status);
        if (status == null || status.trim().isEmpty()) {
            logger.warn("Invalid status update request for order ID: {}. Status is null or empty.", id);
            return ResponseEntity.badRequest().build();
        }
        try {
            CustomerOrder updatedOrder = orderService.updateOrderStatus(id, status);
            logger.info("Order status updated successfully for ID: {}", id);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            logger.error("Error updating status for order ID: {}: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{statusName}")
    public ResponseEntity<List<CustomerOrder>> getOrdersByStatus(@PathVariable String statusName) {
        logger.info("Received request to get orders by status: {}", statusName);
        List<CustomerOrder> orders = orderService.findOrdersByStatus(statusName);
        return ResponseEntity.ok(orders);
    }
}