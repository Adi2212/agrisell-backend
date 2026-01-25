package com.agridev.repository;

import com.agridev.model.OrderStatusHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repository interface for managing order status history records
@Repository
public interface OrderStatusHistoryRepository
        extends JpaRepository<OrderStatusHistory, Long> {

}
