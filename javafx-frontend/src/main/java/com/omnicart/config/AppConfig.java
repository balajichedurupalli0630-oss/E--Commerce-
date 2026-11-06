package com.omnicart.config;

public class AppConfig {
    public static final String APP_NAME = "OmniCart";
    public static final String APP_TAGLINE = "Shop Everything You Need";

    // Service URLs with environment variable / system property overrides
    public static final String PRODUCT_SERVICE_URL = System.getProperty("omnicart.product.url",
            System.getenv().getOrDefault("PRODUCT_SERVICE_URL", "http://localhost:8000"));

    public static final String ORDER_SERVICE_URL = System.getProperty("omnicart.order.url",
            System.getenv().getOrDefault("ORDER_SERVICE_URL", "http://localhost:8001"));

    public static final String PAYMENT_SERVICE_URL = System.getProperty("omnicart.payment.url",
            System.getenv().getOrDefault("PAYMENT_SERVICE_URL", "http://localhost:8002"));

    public static final String AUTH_SERVICE_URL = System.getProperty("omnicart.auth.url",
            System.getenv().getOrDefault("AUTH_SERVICE_URL", "http://localhost:8003"));

    // Stripe Public Key for hosted checkout
    public static final String STRIPE_PUBLISHABLE_KEY = System.getProperty("omnicart.stripe.key",
            System.getenv().getOrDefault("STRIPE_PUBLISHABLE_KEY",
                    "pk_test_51MdCLkDhkeDdZct5FkM9qMlMvAzsJpObS6eUy44jYLuVMhUFjYjzr4VLodA0GiUj0WBaOSzm38QJ8ju3SAYhdNkF00myyAyh6M"));

    public static final int CONNECT_TIMEOUT_SECONDS = 5;
    public static final int REQUEST_TIMEOUT_SECONDS = 10;
}
