package com.example.restaurant.service;

import com.example.restaurant.model.CustomerOrder;
import com.example.restaurant.model.MenuItem;
import com.example.restaurant.model.OrderItem;
import com.example.restaurant.repository.MenuItemRepository;
import com.example.restaurant.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service // <--- ENSURE THIS ANNOTATION IS PRESENT
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<CustomerOrder> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public CustomerOrder createOrder(CustomerOrder orderRequest) {
        CustomerOrder newOrder = new CustomerOrder();
        newOrder.setCustomerName(orderRequest.getCustomerName());
        // orderTime and status are set by @PrePersist in CustomerOrder entity

        // Process order items from the request
        if (orderRequest.getOrderItems() != null) {
            List<OrderItem> processedOrderItems = orderRequest.getOrderItems().stream().map(requestedItem -> {
                if (requestedItem.getMenuItem() == null || requestedItem.getMenuItem().getId() == null) {
                    throw new RuntimeException("MenuItem ID is required for each order item.");
                }
                MenuItem menuItem = menuItemRepository.findById(requestedItem.getMenuItem().getId())
                        .orElseThrow(() -> new RuntimeException("Menu Item not found with ID: " + requestedItem.getMenuItem().getId()));

                OrderItem orderItem = new OrderItem();
                orderItem.setMenuItem(menuItem);
                orderItem.setQuantity(requestedItem.getQuantity());
                orderItem.setUnitPrice(menuItem.getPrice()); // Capture price at time of order
                // The 'newOrder.addOrderItem(orderItem)' will set the bidirectional link
                return orderItem;
            }).collect(Collectors.toList());

            processedOrderItems.forEach(newOrder::addOrderItem); // Add items and set bidirectional link
        }

        newOrder.calculateTotalPrice();
        return orderRepository.save(newOrder);
    }

    @Transactional
    public CustomerOrder updateOrderStatus(Long id, String status) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public List<CustomerOrder> findOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }
}