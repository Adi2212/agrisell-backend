package com.agridev.service;

import com.agridev.dto.*;
import com.agridev.model.*;
import com.agridev.repository.*;

import com.agridev.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

// Service class to handle order related business logic
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final PaymentRepository paymentRepository;

    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;

    // Method to create and place a new order
    public OrderResponse placeOrder(OrderRequest dto, HttpServletRequest request) {

        Order order = new Order();
        order.setStatus(Status.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        order.setUserId(user.getId());
        order.setDeliveryAddress(
                modelMapper.map(user.getAddress(), OrderAddress.class)
        );

        AtomicReference<Double> total = new AtomicReference<>(0.0);

        List<OrderItem> items = dto.getItems().stream().map(i -> {

            Product product = productRepository.findById(i.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Quantity validation before order placement
            if (product.getStockQuantity() < i.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for product: " + product.getName()
                );
            }

            product.setStockQuantity(product.getStockQuantity() - i.getQuantity());
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setQuantity(i.getQuantity());
            item.setPrice(product.getPrice());
            item.setOrder(order);

            total.updateAndGet(v -> v + product.getPrice() * i.getQuantity());

            Address sellerAddress = product.getUser().getAddress();
            item.setPickUpAddress(
                    modelMapper.map(sellerAddress, OrderAddress.class)
            );

            return item;

        }).toList();

        order.setItems(items);
        order.setTotalAmount(total.get());

        Order savedOrder = orderRepository.save(order);
        addHistory(savedOrder, Status.PENDING);

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setAmount(savedOrder.getTotalAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);

        return buildOrderResponse(savedOrder);
    }


    // Method to mark payment as failed
    public OrderResponse markPaymentFailed(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaymentStatus(PaymentStatus.FAILED);

        Payment payment = paymentRepository.findByOrderId(orderId);
        payment.setStatus(PaymentStatus.FAILED);

        paymentRepository.save(payment);
        orderRepository.save(order);

        return buildOrderResponse(order);
    }

    // Method to mark payment as success and confirm order
    public OrderResponse markPaymentSuccess(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(Status.CONFIRMED);

        Payment payment = paymentRepository.findByOrderId(orderId);
        payment.setStatus(PaymentStatus.PAID);

        paymentRepository.save(payment);

        Order saved = orderRepository.save(order);
        addHistory(saved, Status.CONFIRMED);

        return buildOrderResponse(saved);
    }

    // Method to update order delivery status
    public OrderResponse updateStatus(Long orderId, Status status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);

        Order saved = orderRepository.save(order);
        addHistory(saved, status);

        return buildOrderResponse(saved);
    }

    // Method to fetch all orders of logged-in user
    public List<OrderResponse> getUserOrders(HttpServletRequest request) {

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::buildOrderResponse)
                .toList();
    }

    // Method to fetch single order details
    public OrderResponse getOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return buildOrderResponse(order);
    }

    // Helper method to store order status history
    private void addHistory(Order order, Status status) {

        OrderStatusHistory h = new OrderStatusHistory();
        h.setOrder(order);
        h.setStatus(status);

        historyRepository.save(h);
    }

    // Helper method to build order response DTO
    private OrderResponse buildOrderResponse(Order order) {

        OrderResponse r = new OrderResponse();

        r.setOrderId(order.getId());
        r.setTotalAmount(order.getTotalAmount());
        r.setOrderStatus(order.getStatus());
        r.setPaymentStatus(order.getPaymentStatus());
        r.setCreatedAt(order.getCreatedAt());

        r.setDeliveryAddress(
                modelMapper.map(order.getDeliveryAddress(), AddressResponse.class)
        );

        r.setItems(
                order.getItems().stream().map(item -> {

                    OrderItemResponse ir = new OrderItemResponse();

                    ir.setProductId(item.getProductId());
                    ir.setQuantity(item.getQuantity());
                    ir.setPrice(item.getPrice());
                    ir.setLineTotal(item.getPrice() * item.getQuantity());

                    ir.setProductName(
                            productRepository.findById(item.getProductId())
                                    .map(Product::getName)
                                    .orElse("Product")
                    );

                    return ir;

                }).toList()
        );

        return r;
    }
}
