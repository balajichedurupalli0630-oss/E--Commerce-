package com.ecom.product.service;

import com.ecom.dto.ProductRecommendationDTO;
import com.ecom.dto.SkuSalesSummaryDTO;
import com.ecom.product.model.Product;
import com.ecom.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StochasticFinancialEngineService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.order-service.url:${ORDER_SERVICE_URL:http://localhost:8001}}")
    private String orderServiceUrl;

    // Gamma-Poisson Weakly Informative Prior: Gamma(alpha_0 = 2.0, beta_0 = 2.0) => prior mean 1.0 unit/day
    private static final double PRIOR_ALPHA = 2.0;
    private static final double PRIOR_BETA = 2.0;

    // Service level Z-score for 95% in-stock probability
    private static final double Z_SERVICE_LEVEL = 1.65;

    // Hard Financial Guardrails
    private static final double MIN_ACCEPTABLE_MARGIN = 0.10; // Never sell below 10% gross margin
    private static final double MAX_PRICE_STEP = 0.15;        // Max +/- 15% price shift per cycle
    private static final int CONFIDENCE_THRESHOLD = 65;       // Minimum confidence % to emit action

    public List<ProductRecommendationDTO> generateAllRecommendations() {
        List<Product> products = productRepository.findAll();
        Map<Long, SkuSalesSummaryDTO> salesMap = fetchSalesVelocity(30);

        return products.stream()
                .map(product -> analyzeProduct(
                        product,
                        salesMap.getOrDefault(product.getId(), new SkuSalesSummaryDTO(product.getId(), 0, 30))
                ))
                .toList();
    }

    private Map<Long, SkuSalesSummaryDTO> fetchSalesVelocity(int days) {
        try {
            String url = orderServiceUrl + "/api/orders/analytics/sales-velocity?days=" + days;
            List<SkuSalesSummaryDTO> list = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<SkuSalesSummaryDTO>>() {}
            ).getBody();

            if (list == null) return Map.of();
            return list.stream()
                    .filter(s -> s != null && s.getProductId() != null)
                    .collect(Collectors.toMap(SkuSalesSummaryDTO::getProductId, s -> s, (s1, s2) -> s1));
        } catch (Exception e) {
            return Map.of(); // Graceful fallback to prior if order-service is temporarily unreachable
        }
    }

    public ProductRecommendationDTO analyzeProduct(Product product, SkuSalesSummaryDTO sales) {
        double cost = product.getCostPrice() != null ? product.getCostPrice() : 0.0;
        double price = product.getPrice() != null ? product.getPrice() : 0.0;
        int stock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int leadTime = product.getLeadTimeDays() != null ? product.getLeadTimeDays() : 5;

        int unitsSold = sales != null && sales.getTotalUnitsSold() != null ? sales.getTotalUnitsSold() : 0;
        int days = sales != null && sales.getObservationDays() != null ? Math.max(1, sales.getObservationDays()) : 30;

        // 1. Gamma-Poisson Conjugate Posterior: Gamma(alpha_0 + S, beta_0 + T)
        double postAlpha = PRIOR_ALPHA + unitsSold;
        double postBeta = PRIOR_BETA + days;

        // Commons Math uses (shape = alpha, scale = 1 / beta)
        GammaDistribution posterior = new GammaDistribution(postAlpha, 1.0 / postBeta);
        double lambdaMean = postAlpha / postBeta;
        double lambdaVar = postAlpha / (postBeta * postBeta);
        double ciLow = posterior.inverseCumulativeProbability(0.025);
        double ciHigh = posterior.inverseCumulativeProbability(0.975);

        // 2. Confidence Score derived from Posterior Credible Interval Tightness & Sample Volume
        double relativeSpread = (ciHigh - ciLow) / Math.max(0.01, 2.0 * lambdaMean);
        int confidence = (int) Math.round(Math.max(0.0, Math.min(100.0, (1.0 - relativeSpread) * 100.0)));
        if (unitsSold < 5) {
            confidence = Math.min(confidence, 40); // Hard cap confidence when sample size < 5 units
        }

        // 3. Stochastic Reorder Point & Safety Stock
        double leadTimeDemand = lambdaMean * leadTime;
        double safetyStock = Z_SERVICE_LEVEL * Math.sqrt(leadTimeDemand + (lambdaVar * leadTime * leadTime));
        int reorderPoint = (int) Math.ceil(leadTimeDemand + safetyStock);
        double daysOfStockLeft = lambdaMean > 0.01 ? stock / lambdaMean : 999.0;

        String inventoryAction;
        int recommendedBuyQty = 0;
        String inventoryReason;

        if (confidence < CONFIDENCE_THRESHOLD && unitsSold == 0) {
            inventoryAction = "INSUFFICIENT_DATA";
            inventoryReason = "Awaiting initial sales data before forecasting restock.";
        } else if (stock <= reorderPoint && ciLow >= 0.3) {
            inventoryAction = "URGENT_RESTOCK";
            int target30DayStock = (int) Math.ceil(lambdaMean * (30 + leadTime) + safetyStock);
            recommendedBuyQty = Math.max(1, target30DayStock - stock);
            inventoryReason = String.format(
                    "High demand (%.1f units/day, 95%% CI: %.1f–%.1f). Stock (%d) is below stochastic reorder point (%d).",
                    lambdaMean, ciLow, ciHigh, stock, reorderPoint
            );
        } else if (daysOfStockLeft > 90 && stock > 20) {
            inventoryAction = "OVERSTOCKED";
            inventoryReason = String.format(
                    "%.0f days of inventory on hand at current velocity (%.1f/day). Consider promotional discount.",
                    daysOfStockLeft, lambdaMean
            );
        } else {
            inventoryAction = "HEALTHY";
            inventoryReason = String.format("Stock covers ~%.0f days of projected demand.", daysOfStockLeft);
        }

        // 4. Cost-Anchored Dynamic Pricing & Discount Engine
        double currentMargin = price > 0 ? ((price - cost) / price) * 100.0 : 0.0;
        double hardFloorPrice = round2(cost / (1.0 - MIN_ACCEPTABLE_MARGIN));

        String pricingAction = "HOLD";
        double recommendedPrice = price;
        String pricingReason;

        if (cost <= 0) {
            pricingAction = "INSUFFICIENT_DATA";
            pricingReason = "Set buying cost (costPrice) on this product to enable margin optimization.";
        } else if (price < hardFloorPrice) {
            // Immediate loss protection regardless of confidence
            pricingAction = "INCREASE_PRICE";
            recommendedPrice = hardFloorPrice;
            pricingReason = String.format(
                    "Current margin (%.1f%%) is below the 10%% safety floor against ₹%.2f buying cost.",
                    currentMargin, cost
            );
        } else if (confidence < CONFIDENCE_THRESHOLD) {
            pricingAction = "INSUFFICIENT_DATA";
            pricingReason = String.format(
                    "Confidence is %d%% (%d units sold). Need more orders before adjusting price.",
                    confidence, unitsSold
            );
        } else if (currentMargin < 18.0 && ciLow >= 0.8) {
            // Strong lower-bound demand + thin margin => Lerner markup step-up
            double candidate = round2(price * 1.10);
            recommendedPrice = Math.min(round2(price * (1.0 + MAX_PRICE_STEP)), candidate);
            pricingAction = "INCREASE_PRICE";
            pricingReason = String.format(
                    "Strong demand (95%% CI lower bound %.1f/day) supports raising thin %.1f%% margin.",
                    ciLow, currentMargin
            );
        } else if (currentMargin > 38.0 && (daysOfStockLeft > 75 || ciHigh < 0.4)) {
            // High margin + slow-moving inventory => Margin-safe discount to unlock cash
            double candidate = round2(price * 0.88); // 12% discount
            recommendedPrice = Math.max(hardFloorPrice, Math.max(round2(price * (1.0 - MAX_PRICE_STEP)), candidate));
            if (recommendedPrice < price) {
                pricingAction = "APPLY_DISCOUNT";
                pricingReason = String.format(
                        "Slow velocity (%.1f/day) with %.1f%% margin. Discounting accelerates cash recovery while preserving >=10%% margin.",
                        lambdaMean, currentMargin
                );
            } else {
                pricingReason = "Margin is at the safe floor; hold current price.";
            }
        } else {
            pricingReason = String.format("Optimal balance between %.1f%% margin and %.1f units/day velocity.", currentMargin, lambdaMean);
        }

        double projectedMargin = recommendedPrice > 0 ? ((recommendedPrice - cost) / recommendedPrice) * 100.0 : 0.0;

        return ProductRecommendationDTO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .costPrice(cost)
                .currentPrice(price)
                .currentMarginPercent(round2(currentMargin))
                .pricingAction(pricingAction)
                .recommendedPrice(recommendedPrice)
                .projectedMarginPercent(round2(projectedMargin))
                .pricingReason(pricingReason)
                .currentStock(stock)
                .posteriorDailyDemand(round2(lambdaMean))
                .demandCiLow(round2(ciLow))
                .demandCiHigh(round2(ciHigh))
                .daysOfStockLeft(round2(daysOfStockLeft))
                .reorderPoint(reorderPoint)
                .inventoryAction(inventoryAction)
                .recommendedBuyQty(recommendedBuyQty)
                .inventoryReason(inventoryReason)
                .confidencePercent(confidence)
                .dataBasis(String.format("Based on %d units sold across %d days", unitsSold, days))
                .build();
    }

    @Transactional
    public Product applyRecommendedPrice(Long productId, Double newPrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        double cost = product.getCostPrice() != null ? product.getCostPrice() : 0.0;
        double minSafePrice = cost / (1.0 - MIN_ACCEPTABLE_MARGIN);
        if (newPrice < minSafePrice) {
            throw new IllegalArgumentException("Price violates minimum 10% gross margin guardrail.");
        }
        product.setPrice(round2(newPrice));
        return productRepository.save(product);
    }

    @Transactional
    public Product restockProduct(Long productId, Integer addedQty) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        int current = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        product.setStockQuantity(current + Math.max(0, addedQty));
        return productRepository.save(product);
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
