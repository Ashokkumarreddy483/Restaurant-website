package com.example.restaurant.repository;

import com.example.restaurant.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository // <--- ENSURE THIS ANNOTATION IS PRESENT
public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByCustomerName(String customerName);
    List<CustomerOrder> findByStatus(String status);
}