package com.example.restaurant.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER to easily get MenuItem details when fetching OrderItem
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    private int quantity;
    private double unitPrice; // Price of the menu item at the time of order

    @ManyToOne(fetch = FetchType.LAZY) // LAZY to avoid circular dependency issues with default Jackson serialization
    @JoinColumn(name = "customer_order_id", nullable = false)
    @JsonBackReference // Prevents infinite recursion during JSON serialization
    private CustomerOrder customerOrder;
}
