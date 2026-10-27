package com.ecom.payment.service;

import com.ecom.dto.CartItemDTO;
import com.ecom.dto.OrderItemDTO;
import com.ecom.dto.SessionStatusResponse;
import com.ecom.event.PaymentSuccessfulEvent;
import com.stripe.exception.StripeException;
import com.stripe.model.LineItem;
import com.stripe.model.LineItemCollection;
import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${stripe.webhook.secret:whsec_placeholder}")
    private String webhookSecret;

    public StripeService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String createCheckoutSession(List<CartItemDTO> cart, String userId) throws StripeException {
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        if (cart != null) {
            for (CartItemDTO item : cart) {
                long unitAmount = (item.getPrice() != null ? item.getPrice() : 0) * 100L;
                lineItems.add(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(item.getQuantity() != null ? item.getQuantity().longValue() : 1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(unitAmount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(item.getName() != null ? item.getName() : "Product")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                );
            }
        }

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .putExtraParam("ui_mode", "custom")
                .setReturnUrl("http://localhost:3002/return?session_id={CHECKOUT_SESSION_ID}")
                .addAllLineItem(lineItems);

        if (userId != null && !userId.isBlank()) {
            paramsBuilder.setClientReferenceId(userId);
        }

        Session session = Session.create(paramsBuilder.build());
        return session.getClientSecret();
    }

    public SessionStatusResponse getSessionStatus(String sessionId) throws StripeException {
        if (sessionId != null && sessionId.startsWith("mock_")) {
            return new SessionStatusResponse("complete", "paid");
        }
        SessionRetrieveParams params = SessionRetrieveParams.builder()
                .addExpand("line_items")
                .build();
        Session session = Session.retrieve(sessionId, params, null);
        return new SessionStatusResponse(session.getStatus(), session.getPaymentStatus());
    }

    public void publishMockPayment(List<CartItemDTO> cart, String userId, String email) {
        List<OrderItemDTO> products = new ArrayList<>();
        int totalAmount = 0;
        if (cart != null) {
            for (CartItemDTO item : cart) {
                int itemPrice = (item.getPrice() != null ? item.getPrice() : 0);
                int qty = (item.getQuantity() != null ? item.getQuantity() : 1);
                totalAmount += itemPrice * qty * 100;
                products.add(OrderItemDTO.builder()
                        .name(item.getName() != null ? item.getName() : "Product")
                        .quantity(qty)
                        .price(itemPrice * 100)
                        .build());
            }
        }

        PaymentSuccessfulEvent paymentEvent = PaymentSuccessfulEvent.builder()
                .userId(userId != null && !userId.isBlank() ? userId : "guest_user")
                .email(email != null && !email.isBlank() ? email : "customer@example.com")
                .amount(totalAmount)
                .status("success")
                .products(products)
                .build();

        kafkaTemplate.send("payment.successful", paymentEvent);
        log.info("Published mock payment.successful event for user {}", paymentEvent.getUserId());
    }

    public void createStripeProduct(String id, String name, Integer price) {
        try {
            ProductCreateParams params = ProductCreateParams.builder()
                    .setId(id)
                    .setName(name)
                    .setDefaultPriceData(
                            ProductCreateParams.DefaultPriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount((price != null ? price : 0) * 100L)
                                    .build()
                    )
                    .build();
            Product product = Product.create(params);
            log.info("Created Stripe product: {}", product.getId());
        } catch (Exception e) {
            log.warn("Could not create product in Stripe: {}", e.getMessage());
        }
    }

    public void deleteStripeProduct(String id) {
        try {
            Product product = Product.retrieve(id);
            product.delete();
            log.info("Deleted Stripe product: {}", id);
        } catch (Exception e) {
            log.warn("Could not delete product in Stripe: {}", e.getMessage());
        }
    }

    public void handleStripeWebhook(String payload, String sigHeader) {
        try {
            com.stripe.model.Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null) {
                    LineItemCollection lineItems = session.listLineItems();
                    List<OrderItemDTO> products = new ArrayList<>();
                    if (lineItems != null && lineItems.getData() != null) {
                        for (LineItem item : lineItems.getData()) {
                            products.add(OrderItemDTO.builder()
                                    .name(item.getDescription())
                                    .quantity(item.getQuantity() != null ? item.getQuantity().intValue() : 1)
                                    .price(item.getPrice() != null && item.getPrice().getUnitAmount() != null ? item.getPrice().getUnitAmount().intValue() : 0)
                                    .build());
                        }
                    }

                    PaymentSuccessfulEvent paymentEvent = PaymentSuccessfulEvent.builder()
                            .userId(session.getClientReferenceId())
                            .email(session.getCustomerDetails() != null ? session.getCustomerDetails().getEmail() : "customer@example.com")
                            .amount(session.getAmountTotal() != null ? session.getAmountTotal().intValue() : 0)
                            .status("paid".equalsIgnoreCase(session.getPaymentStatus()) ? "success" : "failed")
                            .products(products)
                            .build();

                    kafkaTemplate.send("payment.successful", paymentEvent);
                    log.info("Published payment.successful event for session {}", session.getId());
                }
            }
        } catch (Exception e) {
            log.error("Stripe webhook verification or processing error: {}", e.getMessage());
            throw new RuntimeException("Webhook error: " + e.getMessage());
        }
    }
}
