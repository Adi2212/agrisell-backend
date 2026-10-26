package com.agridev.service;

import java.util.List;
import java.util.ArrayList;

import com.agridev.dto.*;
import com.agridev.model.*;
import com.agridev.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

// Service class to handle admin related operations with pagination
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    private final ModelMapper modelMapper;

    // Fetch paginated farmer list
    public Page<FarmerAdminResponse> getFarmers(Pageable pageable) {

        log.info("Fetching farmers list with pagination");

        return userRepository.findByRole(Role.FARMER, pageable)
                .map(farmer -> {
                    FarmerAdminResponse res = new FarmerAdminResponse();
                    res.setId(farmer.getId());
                    res.setName(farmer.getName());
                    res.setPhone(farmer.getPhone());
                    res.setAccStatus(farmer.getAccStatus());
                    res.setProductCount(productRepository.countByUserId(farmer.getId()));
                    return res;
                });
    }

    // Toggle farmer account status
    public void toggleFarmerStatus(Long id) {

        log.info("Toggling farmer status for farmerId: {}", id);

        User farmer = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Farmer not found with id: {}", id);
                    return new RuntimeException("Farmer not found");
                });

        if (farmer.getAccStatus().equals(AccStatus.ACTIVE)) {
            farmer.setAccStatus(AccStatus.INACTIVE);
            log.info("Farmer account deactivated for id: {}", id);
        } else {
            farmer.setAccStatus(AccStatus.ACTIVE);
            log.info("Farmer account activated for id: {}", id);
        }

        userRepository.save(farmer);
    }

    // Fetch paginated buyer list
    public Page<BuyerAdminResponse> getBuyers(Pageable pageable) {

        log.info("Fetching buyers list with pagination");

        return userRepository.findByRole(Role.BUYER, pageable)
                .map(buyer -> {
                    BuyerAdminResponse res = new BuyerAdminResponse();
                    res.setId(buyer.getId());
                    res.setName(buyer.getName());
                    res.setEmail(buyer.getEmail());
                    res.setAccStatus(buyer.getAccStatus());

                    res.setOrderCount(orderRepository.countByUserId(buyer.getId()));
                    res.setTotalSpent(orderRepository.sumTotalByUserId(buyer.getId()));

                    return res;
                });
    }

    // Block or activate buyer account
    public void blockBuyer(Long id) {

        log.info("Toggling buyer status for buyerId: {}", id);

        User buyer = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Buyer not found with id: {}", id);
                    return new RuntimeException("Buyer not found");
                });

        if (buyer.getAccStatus().equals(AccStatus.ACTIVE)) {
            buyer.setAccStatus(AccStatus.INACTIVE);
            log.info("Buyer account blocked for id: {}", id);
        } else {
            buyer.setAccStatus(AccStatus.ACTIVE);
            log.info("Buyer account activated for id: {}", id);
        }

        userRepository.save(buyer);
    }

    // Fetch paginated products list with Offer info
    public Page<ProductAdminResponse> getProducts(Pageable pageable) {

        log.info("Fetching product list with pagination");

        return productRepository.findAll(pageable)
                .map(product -> {
                    ProductAdminResponse res = new ProductAdminResponse();
                    res.setId(product.getId());
                    res.setName(product.getName());
                    res.setFarmerName(product.getUser() != null ? product.getUser().getName() : "Unknown");
                    res.setPrice(product.getPrice());
                    res.setStock(product.getStockQuantity());

                    if (product.getOffer() != null && product.getOffer().isActive()) {
                        res.setOfferTitle(product.getOffer().getTitle());
                        res.setDiscountPercentage(product.getOffer().getDiscountPercentage());
                        double discount = product.getOffer().getDiscountPercentage() != null ? product.getOffer().getDiscountPercentage() : 0.0;
                        double discounted = Math.round((product.getPrice() * (1.0 - (discount / 100.0))) * 100.0) / 100.0;
                        res.setDiscountedPrice(discounted);
                    } else {
                        res.setDiscountedPrice(product.getPrice());
                    }

                    return res;
                });
    }

    // Approve product listing
    public void approveProduct(Long id) {

        log.info("Approving product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with id: {}", id);
                    return new RuntimeException("Product not found");
                });

        productRepository.save(product);

        log.info("Product approved successfully with id: {}", id);
    }

    // Delete product listing
    public void deleteProduct(Long id) {

        log.info("Deleting product with id: {}", id);

        if (!productRepository.existsById(id)) {
            log.error("Cannot delete product. Product not found with id: {}", id);
            throw new RuntimeException("Product not found");
        }

        productRepository.deleteById(id);

        log.info("Product deleted successfully with id: {}", id);
    }

    // Fetch paginated orders list
    public Page<OrderAdminResponse> getOrders(Pageable pageable) {

        log.info("Fetching orders list with pagination");

        return orderRepository.findAll(pageable)
                .map(order -> {
                    OrderAdminResponse res = new OrderAdminResponse();
                    res.setId(order.getId());
                    res.setTotalAmount(order.getTotalAmount());
                    res.setStatus(order.getStatus() != null ? order.getStatus().name() : "PENDING");
                    res.setCreatedAt(order.getCreatedAt());

                    res.setBuyerName(
                            userRepository.findById(order.getUserId())
                                    .map(User::getName)
                                    .orElse("Unknown")
                    );

                    return res;
                });
    }

    // Fetch order details by id
    public OrderDetailsAdminResponse getOrderDetails(Long id) {

        log.info("Fetching order details for orderId: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", id);
                    return new RuntimeException("Order not found");
                });

        OrderDetailsAdminResponse res =
                modelMapper.map(order, OrderDetailsAdminResponse.class);

        res.setStatus(order.getStatus() != null ? order.getStatus().name() : "PENDING");

        res.setBuyerName(
                userRepository.findById(order.getUserId())
                        .map(User::getName)
                        .orElse("Unknown")
        );

        res.setItems(
                order.getItems().stream()
                        .map(item -> {
                            OrderItemAdminResponse dto =
                                    modelMapper.map(item, OrderItemAdminResponse.class);

                            productRepository.findById(item.getProduct().getId())
                                    .ifPresent(p -> dto.setProductName(p.getName()));

                            return dto;
                        })
                        .toList()
        );

        // Explicitly map history to guarantee timestamps are preserved and eliminate consecutive duplicate statuses
        if (order.getHistory() != null) {
            List<OrderStatusHistoryResponse> cleanHistory = new ArrayList<>();
            String lastStatus = null;
            for (OrderStatusHistory h : order.getHistory()) {
                String currentStatus = h.getStatus() != null ? h.getStatus().name() : "";
                if (!currentStatus.equals(lastStatus)) {
                    OrderStatusHistoryResponse dto = new OrderStatusHistoryResponse();
                    dto.setId(h.getId());
                    dto.setStatus(currentStatus);
                    dto.setChangedAt(h.getCreatedAt());
                    dto.setCreatedAt(h.getCreatedAt());
                    cleanHistory.add(dto);
                    lastStatus = currentStatus;
                }
            }
            res.setHistory(cleanHistory);
        }

        log.info("Order details fetched successfully for orderId: {}", id);

        return res;
    }

    // Update order status and store history
    public void updateOrderStatus(Long id, String status) {

        log.info("Updating order status for orderId: {} to status: {}", id, status);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", id);
                    return new RuntimeException("Order not found");
                });

        Status newStatus = Status.valueOf(status.toUpperCase());

        // Skip if order is already in the requested status to prevent duplicate history records
        if (order.getStatus() == newStatus) {
            log.info("Order {} is already in status {}. Skipping duplicate status update.", id, newStatus);
            return;
        }

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        // Check latest history entry to prevent duplicate history rows
        if (savedOrder.getHistory() != null && !savedOrder.getHistory().isEmpty()) {
            OrderStatusHistory last = savedOrder.getHistory().get(savedOrder.getHistory().size() - 1);
            if (last.getStatus() == newStatus) {
                log.info("Order {} already has latest history as {}. Skipping duplicate history entry.", id, newStatus);
                return;
            }
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setStatus(newStatus);
        history.setOrder(savedOrder);

        OrderStatusHistory savedH = orderStatusHistoryRepository.save(history);
        if (savedOrder.getHistory() != null) {
            savedOrder.getHistory().add(savedH);
        }

        log.info("Order status updated successfully for orderId: {}", id);
    }

    // Fetch admin dashboard stats
    public AdminStatsResponse getDashboardStats() {

        log.info("Fetching admin dashboard statistics");

        long farmers = userRepository.countByRole(Role.FARMER);
        long buyers = userRepository.countByRole(Role.BUYER);
        long products = productRepository.count();
        long orders = orderRepository.count();

        double revenue = orderRepository.totalRevenue();

        log.info("Dashboard stats fetched successfully");

        return new AdminStatsResponse(farmers, buyers, products, orders, revenue);
    }
}
