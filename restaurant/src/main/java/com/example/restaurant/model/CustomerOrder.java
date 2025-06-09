package com.example.restaurant.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "customer_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private LocalDateTime orderTime;
    private double totalPrice;
    private String status;

    // CascadeType.ALL: if an order is deleted, its items are also deleted.
    // orphanRemoval=true: if an OrderItem is removed from the orderItems list, it's deleted from the DB.
    // FetchType.EAGER: Load order items along with the order. For many items, LAZY might be better.
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "customerOrder", orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.orderTime = LocalDateTime.now();
        if (this.status == null || this.status.trim().isEmpty()) {
            this.status = "PENDING";
        }
    }

    public void calculateTotalPrice() {
        this.totalPrice = this.orderItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }

    // Helper method to add an order item and set the bidirectional relationship
    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        item.setCustomerOrder(this);
    }
}