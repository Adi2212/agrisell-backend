package com.agridev.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Auditable;

import java.io.Serializable;
import java.time.LocalDateTime;

// Entity class to store payment transaction details
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity{

    // Link payment with order
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Transaction id from gateway (Razorpay/Stripe)
    private String transactionId;

    // Payment method like UPI, CARD, COD
    private String paymentMethod;

    // Amount paid
    private Double amount;

    // Payment status
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;


}
