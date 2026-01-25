package com.agridev.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Entity class to store order details
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    private Long userId;

    private Double totalAmount;

    // Order delivery status
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    // Payment status only (PAID, FAILED, PENDING)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Embedded
    private OrderAddress deliveryAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderStatusHistory> history = new ArrayList<>();

    // One order can have one payment record
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

}
