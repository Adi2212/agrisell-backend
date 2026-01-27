package com.agridev.service;

import com.agridev.dto.OrderItemRequest;
import com.agridev.dto.StripeResponse;
import com.agridev.model.Order;
import com.agridev.model.Payment;
import com.agridev.model.PaymentStatus;
import com.agridev.model.Product;
import com.agridev.repository.PaymentRepository;
import com.agridev.repository.ProductRepository;
import com.agridev.repository.OrderRepository;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// Service class to handle Stripe payment checkout
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    // Initialize Stripe secret key
    @PostConstruct
    public void init() {

        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe secret key initialized successfully");
    }

    // Create Stripe checkout session and save transaction id
    public StripeResponse checkout(Long orderId, List<OrderItemRequest> items) {

        log.info("Checkout request received for orderId: {}", orderId);

        try {

            SessionCreateParams.Builder params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(
                                    "http://localhost:5173/payment/success?orderId=" + orderId
                            )
                            .setCancelUrl(
                                    "http://localhost:5173/payment/cancel?orderId=" + orderId
                            );

            for (OrderItemRequest item : items) {

                Long productId = item.getProduct().getProductId();

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> {
                            log.error("Product not found during checkout. productId: {}", productId);
                            return new RuntimeException("Product not found");
                        });

                log.debug("Adding product to Stripe session: {} (qty: {})",
                        product.getName(),
                        item.getQuantity()
                );

                SessionCreateParams.LineItem.PriceData.ProductData productData =
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(product.getName())
                                .build();

                SessionCreateParams.LineItem.PriceData priceData =
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("inr")
                                .setUnitAmount(Math.round(product.getPrice() * 100))
                                .setProductData(productData)
                                .build();

                SessionCreateParams.LineItem lineItem =
                        SessionCreateParams.LineItem.builder()
                                .setQuantity((long) item.getQuantity())
                                .setPriceData(priceData)
                                .build();

                params.addLineItem(lineItem);
            }

            // Create Stripe session
            Session session = Session.create(params.build());

            log.info("Stripe checkout session created successfully for orderId: {}", orderId);

            // Save session id in Payment table
            Payment payment = paymentRepository.findByOrderId(orderId);

            payment.setTransactionId(session.getId());
            payment.setStatus(PaymentStatus.PENDING);

            paymentRepository.save(payment);

            log.info("Payment transaction updated with Stripe sessionId: {} for orderId: {}",
                    session.getId(), orderId);

            return StripeResponse.builder()
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .status("SUCCESS")
                    .build();

        } catch (StripeException e) {

            log.error("Stripe checkout failed for orderId: {}. Error: {}", orderId, e.getMessage());

            return StripeResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build();
        }
    }

    // Retry Payment Logic
    public Map<String, String> retryPayment(Long orderId) {

        log.info("Retry payment request received for orderId: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found during retry payment. orderId: {}", orderId);
                    return new RuntimeException("Order not found");
                });

        // Allow retry only if payment failed
        if (order.getPaymentStatus() != PaymentStatus.FAILED) {

            log.warn("Payment retry not allowed. Current status: {} for orderId: {}",
                    order.getPaymentStatus(), orderId);

            throw new RuntimeException("Payment retry not allowed");
        }

        try {

            Stripe.apiKey = stripeSecretKey;

            log.info("Creating Stripe retry session for orderId: {}", orderId);

            Session session = Session.create(
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(
                                    "http://localhost:5173/payment/success?orderId=" + orderId
                            )
                            .setCancelUrl(
                                    "http://localhost:5173/payment/cancel?orderId=" + orderId
                            )
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency("inr")
                                                            .setUnitAmount(Math.round(order.getTotalAmount() * 100))
                                                            .setProductData(
                                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                            .setName("Order #" + order.getId())
                                                                            .build()
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );

            log.info("Stripe retry session created successfully for orderId: {}", orderId);

            return Map.of("sessionUrl", session.getUrl());

        } catch (Exception e) {

            log.error("Stripe retry payment failed for orderId: {}. Error: {}",
                    orderId, e.getMessage());

            throw new RuntimeException("Stripe retry payment failed: " + e.getMessage());
        }
    }
}
