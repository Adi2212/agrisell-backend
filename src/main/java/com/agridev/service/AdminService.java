package com.agridev.service;

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

    // Fetch paginated products list
    public Page<ProductAdminResponse> getProducts(Pageable pageable) {

        log.info("Fetching product list with pagination");

        return productRepository.findAll(pageable)
                .map(product -> {
                    ProductAdminResponse res = new ProductAdminResponse();
                    res.setId(product.getId());
                    res.setName(product.getName());
                    res.setFarmerName(product.getUser().getName());
                    res.setPrice(product.getPrice());
                    res.setStock(product.getStockQuantity());
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
                    res.setStatus(order.getStatus().name());
                    res.setCreatedAt(order.getCreatedAt().toString());

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

        res.setHistory(
                order.getHistory().stream()
                        .map(h -> modelMapper.map(h, OrderStatusHistoryResponse.class))
                        .toList()
        );

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
        order.setStatus(newStatus);

        orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setStatus(newStatus);
        history.setOrder(order);

        orderStatusHistoryRepository.save(history);

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
