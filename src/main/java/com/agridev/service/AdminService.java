package com.agridev.service;

import com.agridev.dto.*;
import com.agridev.model.*;
import com.agridev.repository.*;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

// Service class to handle admin related operations with pagination
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    private final ModelMapper modelMapper;

    // Method to fetch paginated farmer list
    public Page<FarmerAdminResponse> getFarmers(Pageable pageable) {

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

    // Method to toggle farmer account status
    public void toggleFarmerStatus(Long id) {

        User farmer = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        if (farmer.getAccStatus().equals(AccStatus.ACTIVE)) {
            farmer.setAccStatus(AccStatus.INACTIVE);
        } else {
            farmer.setAccStatus(AccStatus.ACTIVE);
        }

        userRepository.save(farmer);
    }

    // Method to fetch paginated buyer list
    public Page<BuyerAdminResponse> getBuyers(Pageable pageable) {

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

    // Method to block or activate buyer account
    public void blockBuyer(Long id) {

        User buyer = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        if (buyer.getAccStatus().equals(AccStatus.ACTIVE)) {
            buyer.setAccStatus(AccStatus.INACTIVE);
        } else {
            buyer.setAccStatus(AccStatus.ACTIVE);
        }

        userRepository.save(buyer);
    }

    // Method to fetch paginated products list
    public Page<ProductAdminResponse> getProducts(Pageable pageable) {

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

    // Method to approve product listing
    public void approveProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepository.save(product);
    }

    // Method to delete product listing
    public void deleteProduct(Long id) {

        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }

        productRepository.deleteById(id);
    }

    // Method to fetch paginated orders list
    public Page<OrderAdminResponse> getOrders(Pageable pageable) {

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

    // Method to fetch order details by id
    public OrderDetailsAdminResponse getOrderDetails(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

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

        return res;
    }

    // Method to update order status and store history
    public void updateOrderStatus(Long id, String status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Status newStatus = Status.valueOf(status.toUpperCase());
        order.setStatus(newStatus);

        orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setStatus(newStatus);
        history.setOrder(order);

        orderStatusHistoryRepository.save(history);
    }

    // Method to fetch admin dashboard stats
    public AdminStatsResponse getDashboardStats() {

        long farmers = userRepository.countByRole(Role.FARMER);
        long buyers = userRepository.countByRole(Role.BUYER);
        long products = productRepository.count();
        long orders = orderRepository.count();

        double revenue = orderRepository.totalRevenue();

        return new AdminStatsResponse(farmers, buyers, products, orders, revenue);
    }

}
