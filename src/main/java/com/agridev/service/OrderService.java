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
           item.setProduct(product);
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

    // Cancel Order Logic
    public String cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Restrict cancellation after shipping
        if (order.getStatus() == Status.SHIPPED ||
                order.getStatus() == Status.DELIVERED) {

            throw new RuntimeException("Order cannot be cancelled after shipping");
        }

        // If already cancelled
        if (order.getStatus() == Status.CANCELLED) {
            return "Order is already cancelled";
        }

        // Cancel the order
        order.setStatus(Status.CANCELLED);
        order.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            product.setStockQuantity(
                    product.getStockQuantity() + item.getQuantity()
            );
        });

        orderRepository.save(order);

        return "Order Cancelled Successfully";
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

    // Get Orders for Farmer
    public List<OrderResponse> getOrdersForFarmer(HttpServletRequest request) {

        String token = jwtUtil.extractToken(request);
        Long farmerId = jwtUtil.extractUserId(token);

        List<Order> orders = orderRepository.findOrdersByFarmerId(farmerId);

        return orders.stream().map(order -> {


            List<OrderItem> farmerItems = order.getItems().stream()
                    .filter(i -> i.getProduct().getUser().getId().equals(farmerId))
                    .toList();

            order.setItems(farmerItems);

            return buildOrderResponse(order);

        }).toList();
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

    private OrderResponse buildOrderResponse(Order order) {

        OrderResponse r = new OrderResponse();

        r.setOrderId(order.getId());
        r.setTotalAmount(order.getTotalAmount());
        r.setOrderStatus(order.getStatus());
        r.setPaymentStatus(order.getPaymentStatus());
        r.setCreatedAt(order.getCreatedAt());

        // Address Mapping
        r.setDeliveryAddress(
                modelMapper.map(order.getDeliveryAddress(), AddressResponse.class)
        );

        // Items Mapping
        r.setItems(
                order.getItems().stream().map(item -> {

                    OrderItemResponse ir = new OrderItemResponse();

                    // Build ProductItem DTO
                    ProductItem p = new ProductItem();
                    p.setProductId(item.getProduct().getId());
                    p.setProductName(item.getProduct().getName());

                    // Set product DTO
                    ir.setProduct(p);

                    // Set quantity & price
                    ir.setQuantity(item.getQuantity());
                    ir.setPrice(item.getPrice());

                    // Calculate line total
                    ir.setLineTotal(item.getPrice() * item.getQuantity());

                    return ir;

                }).toList()
        );

        return r;
    }

}
