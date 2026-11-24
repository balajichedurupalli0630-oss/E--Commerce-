package com.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecommendationDTO {
    private Long productId;
    private String productName;

    // Financials
    private Double costPrice;
    private Double currentPrice;
    private Double currentMarginPercent;

    // Pricing Recommendation
    private String pricingAction;        // "INCREASE_PRICE", "APPLY_DISCOUNT", "HOLD", "INSUFFICIENT_DATA"
    private Double recommendedPrice;
    private Double projectedMarginPercent;
    private String pricingReason;

    // Stochastic Demand & Inventory
    private Integer currentStock;
    private Double posteriorDailyDemand; // Gamma-Poisson posterior mean (lambda_hat)
    private Double demandCiLow;          // 95% Credible Interval Lower Bound
    private Double demandCiHigh;         // 95% Credible Interval Upper Bound
    private Double daysOfStockLeft;
    private Integer reorderPoint;
    private String inventoryAction;      // "URGENT_RESTOCK", "HEALTHY", "OVERSTOCKED", "INSUFFICIENT_DATA"
    private Integer recommendedBuyQty;
    private String inventoryReason;

    // Confidence & Auditability
    private Integer confidencePercent;   // 0 to 100
    private String dataBasis;            // e.g., "Based on 84 units sold over 30 days"
}
