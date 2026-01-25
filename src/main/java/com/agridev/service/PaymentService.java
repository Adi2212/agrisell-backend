package com.agridev.service;

import com.agridev.dto.OrderItemRequest;
import com.agridev.dto.StripeResponse;
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

// Service class to handle Stripe payment checkout
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    // Method to initialize Stripe secret key
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    // Method to create Stripe checkout session and save session id
    public StripeResponse checkout(Long orderId, List<OrderItemRequest> items) {

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

                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

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

            // Save session id in Payment table
            Payment payment = paymentRepository.findByOrderId(orderId);

            payment.setTransactionId(session.getId());
            payment.setStatus(PaymentStatus.PENDING);

            paymentRepository.save(payment);

            return StripeResponse.builder()
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .status("SUCCESS")
                    .build();

        } catch (StripeException e) {

            return StripeResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build();
        }
    }
}
