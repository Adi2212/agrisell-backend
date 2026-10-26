package com.agridev.service;

import com.agridev.dto.*;
import com.agridev.model.*;
import com.agridev.repository.*;
import com.agridev.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final PaymentRepository paymentRepository;
    private final org.springframework.context.ApplicationContext applicationContext;
    private final NotificationService notificationService;
    private final CartService cartService;

    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;

    // Place a new order
    public OrderResponse placeOrder(OrderRequest dto, HttpServletRequest request) {

        log.info("Place order request received");

        Order order = new Order();
        order.setStatus(Status.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Order placement failed: User not found with id {}", userId);
                    return new RuntimeException("User not found");
                });

        log.info("Placing order for userId: {}", userId);

        order.setUserId(user.getId());
        order.setDeliveryAddress(
                modelMapper.map(user.getAddress(), OrderAddress.class)
        );

        AtomicReference<Double> total = new AtomicReference<>(0.0);

        List<OrderItem> items = dto.getItems().stream().map(i -> {

            Product product = productRepository.findById(i.getProduct().getProductId())
                    .orElseThrow(() -> {
                        log.error("Product not found with id {}", i.getProduct().getProductId());
                        return new RuntimeException("Product not found");
                    });

            if (product.getStockQuantity() < i.getQuantity()) {
                log.warn("Insufficient stock for product {}. Available: {}, Requested: {}",
                        product.getName(),
                        product.getStockQuantity(),
                        i.getQuantity());
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
        log.info("Order created successfully with orderId: {}", savedOrder.getId());

        addHistory(savedOrder, Status.PENDING);

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setAmount(savedOrder.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);

        log.info("Payment entry created for orderId: {}", savedOrder.getId());
        
        notificationService.createNotification(
            userId, 
            "Order Placed", 
            "Your order #" + savedOrder.getId() + " has been placed successfully but is pending payment."
        );

        return buildOrderResponse(savedOrder);
    }

    // Cancel Order
    public String cancelOrder(Long orderId) {

        log.info("Cancel order request received for orderId: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with id {}", orderId);
                    return new RuntimeException("Order not found");
                });

        if (order.getStatus() == Status.SHIPPED ||
                order.getStatus() == Status.DELIVERED) {

            log.warn("Order cannot be cancelled after shipping. orderId: {}", orderId);
            throw new RuntimeException("Order cannot be cancelled after shipping");
        }

        if (order.getStatus() == Status.CANCELLED) {
            log.info("Order already cancelled. orderId: {}", orderId);
            return "Order is already cancelled";
        }

        order.setStatus(Status.CANCELLED);

        order.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        });

        Order saved = orderRepository.save(order);
        addHistory(saved, Status.CANCELLED);

        log.info("Order cancelled successfully with orderId: {}", orderId);
        
        notificationService.createNotification(
            order.getUserId(), 
            "Order Cancelled", 
            "Your order #" + order.getId() + " has been cancelled."
        );

        return "Order cancelled successfully";
    }

    // Mark payment failed
    public OrderResponse markPaymentFailed(Long orderId) {

        log.info("Mark payment failed for orderId: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setStatus(Status.CANCELLED);

        Payment payment = paymentRepository.findByOrderId(orderId);
        payment.setStatus(PaymentStatus.FAILED);

        paymentRepository.save(payment);
        addHistory(order, Status.CANCELLED);

        orderRepository.save(order);

        log.info("Payment marked failed successfully for orderId: {}", orderId);
        
        notificationService.createNotification(
            order.getUserId(), 
            "Payment Failed", 
            "Payment for order #" + order.getId() + " has failed. Please retry."
        );

        return buildOrderResponse(order);
    }

    // Mark payment success
    public OrderResponse markPaymentSuccess(Long orderId) {

        log.info("Mark payment success for orderId: {}", orderId);

        // Verify with Stripe before marking as success
        PaymentService paymentService = applicationContext.getBean(PaymentService.class);
        boolean isVerified = paymentService.verifyPayment(orderId);
        
        if (!isVerified) {
            log.error("Stripe payment verification failed for orderId: {}", orderId);
            throw new RuntimeException("Payment verification failed! Order not paid.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("Order {} is already marked as paid. Skipping duplicate processing.", orderId);
            return buildOrderResponse(order);
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(Status.CONFIRMED);

        Payment payment = paymentRepository.findByOrderId(orderId);
        payment.setStatus(PaymentStatus.PAID);

        paymentRepository.save(payment);

        Order saved = orderRepository.save(order);
        addHistory(saved, Status.CONFIRMED);

        log.info("Payment successful and order confirmed for orderId: {}", orderId);

        cartService.clearCart(order.getUserId());

        notificationService.createNotification(
            order.getUserId(), 
            "Payment Successful", 
            "Payment for order #" + order.getId() + " was successful. Your order is now confirmed!"
        );

        return buildOrderResponse(saved);
    }

    // Update order status
    public OrderResponse updateStatus(Long orderId, Status status) {

        log.info("Updating order status for orderId: {} to status: {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with id {}", orderId);
                    return new RuntimeException("Order not found");
                });

        if (order.getStatus() == status) {
            log.info("Order {} is already in status {}. Skipping duplicate status update.", orderId, status);
            return buildOrderResponse(order);
        }

        order.setStatus(status);

        Order updated = orderRepository.save(order);
        addHistory(updated, status);

        log.info("Order status updated successfully for orderId: {}", orderId);
        
        notificationService.createNotification(
            order.getUserId(), 
            "Order Status Updated", 
            "Your order #" + order.getId() + " is now " + status.name() + "."
        );

        return buildOrderResponse(updated);
    }

    // Fetch logged in user orders
    public List<OrderResponse> getUserOrders(HttpServletRequest request) {

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        log.info("Fetching orders for userId: {}", userId);

        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::buildOrderResponse)
                .toList();
    }

    // Fetch orders containing products of logged-in farmer
    public List<OrderResponse> getOrdersForFarmer(HttpServletRequest request) {

        Long farmerId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        log.info("Fetching orders for farmerId: {}", farmerId);

        List<Order> orders = orderRepository.findOrdersByFarmerId(farmerId);

        return orders.stream().map(order -> {

            List<OrderItem> farmerItems = order.getItems().stream()
                    .filter(i -> i.getProduct().getUser().getId().equals(farmerId))
                    .toList();

            order.setItems(farmerItems);

            return buildOrderResponse(order);

        }).toList();
    }

    // Fetch single order details
    public OrderResponse getOrder(Long id) {

        log.info("Fetching order details for orderId: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id {}", id);
                    return new RuntimeException("Order not found");
                });

        return buildOrderResponse(order);
    }

    // Helper: Store order status history
    private void addHistory(Order order, Status status) {

        log.debug("Adding order history for orderId: {}, status: {}", order.getId(), status);

        // Prevent duplicate consecutive history entries for the same status
        if (order.getHistory() != null && !order.getHistory().isEmpty()) {
            OrderStatusHistory last = order.getHistory().get(order.getHistory().size() - 1);
            if (last.getStatus() == status) {
                log.debug("Order {} already has latest status history as {}. Skipping duplicate history entry.", order.getId(), status);
                return;
            }
        }

        OrderStatusHistory h = new OrderStatusHistory();
        h.setOrder(order);
        h.setStatus(status);

        OrderStatusHistory savedH = historyRepository.save(h);
        if (order.getHistory() != null) {
            order.getHistory().add(savedH);
        }
    }

    // Order stats for graph
    public List<OrderStatusStatsResponse> getOrderStatusStats(Long days) {

        log.info("Fetching order status stats for last {} days", days);

        LocalDateTime start = LocalDate.now()
                .minusDays(days)
                .atStartOfDay();

        LocalDateTime end = LocalDate.now()
                .atTime(LocalTime.MAX);

        List<Object[]> rows = orderRepository.countByDateAndStatus(start, end);

        Map<LocalDate, Map<String, Long>> map = new LinkedHashMap<>();

        LocalDate d = LocalDate.now().minusDays(days);
        while (!d.isAfter(LocalDate.now())) {

            Map<String, Long> inner = new HashMap<>();
            inner.put("PENDING", 0L);
            inner.put("CONFIRMED", 0L);
            inner.put("SHIPPED", 0L);
            inner.put("DELIVERED", 0L);
            inner.put("CANCELLED", 0L);

            map.put(d, inner);
            d = d.plusDays(1);
        }

        for (Object[] row : rows) {

            LocalDate rowDate;
            if (row[0] instanceof java.sql.Date sqlDate) {
                rowDate = sqlDate.toLocalDate();
            } else {
                rowDate = LocalDate.parse(row[0].toString());
            }

            String status = row[1].toString();

            Long count = (row[2] instanceof BigInteger bi)
                    ? bi.longValue()
                    : Long.parseLong(row[2].toString());

            map.get(rowDate).put(status, count);
        }

        List<OrderStatusStatsResponse> response = new ArrayList<>();

        for (Map.Entry<LocalDate, Map<String, Long>> entry : map.entrySet()) {

            Map<String, Long> c = entry.getValue();

            response.add(new OrderStatusStatsResponse(
                    entry.getKey().toString(),
                    c.get("PENDING"),
                    c.get("CONFIRMED"),
                    c.get("SHIPPED"),
                    c.get("DELIVERED"),
                    c.get("CANCELLED")
            ));
        }

        log.info("Order status stats generated successfully");

        return response;
    }

    // Build Order Response DTO
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

                    ProductItem p = new ProductItem();
                    p.setProductId(item.getProduct().getId());
                    p.setProductName(item.getProduct().getName());

                    ir.setProduct(p);
                    ir.setQuantity(item.getQuantity());
                    ir.setPrice(item.getPrice());
                    ir.setLineTotal(item.getPrice() * item.getQuantity());

                    return ir;

                }).toList()
        );

        // Populate order status history with explicit timestamps and filter out consecutive duplicate statuses
        if (order.getHistory() != null) {
            List<OrderStatusHistoryResponse> cleanHistory = new ArrayList<>();
            String lastStatus = null;
            for (OrderStatusHistory h : order.getHistory()) {
                String currentStatus = h.getStatus() != null ? h.getStatus().name() : "";
                if (!currentStatus.equals(lastStatus)) {
                    OrderStatusHistoryResponse hr = new OrderStatusHistoryResponse();
                    hr.setId(h.getId());
                    hr.setStatus(currentStatus);
                    hr.setChangedAt(h.getCreatedAt());
                    hr.setCreatedAt(h.getCreatedAt());
                    cleanHistory.add(hr);
                    lastStatus = currentStatus;
                }
            }
            r.setHistory(cleanHistory);
        }

        return r;
    }
}
