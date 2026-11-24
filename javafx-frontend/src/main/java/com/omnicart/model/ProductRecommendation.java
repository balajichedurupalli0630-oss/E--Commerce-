package com.omnicart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRecommendation {
    private Long productId;
    private String productName;
    private Double costPrice;
    private Double currentPrice;
    private Double currentMarginPercent;

    private String pricingAction;        // INCREASE_PRICE, APPLY_DISCOUNT, HOLD, INSUFFICIENT_DATA
    private Double recommendedPrice;
    private Double projectedMarginPercent;
    private String pricingReason;

    private Integer currentStock;
    private Double posteriorDailyDemand;
    private Double demandCiLow;
    private Double demandCiHigh;
    private Double daysOfStockLeft;
    private Integer reorderPoint;
    private String inventoryAction;      // URGENT_RESTOCK, HEALTHY, OVERSTOCKED, INSUFFICIENT_DATA
    private Integer recommendedBuyQty;
    private String inventoryReason;

    private Integer confidencePercent;
    private String dataBasis;

    public ProductRecommendation() {}

    // Standard Getters & Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Double getCostPrice() { return costPrice; }
    public void setCostPrice(Double costPrice) { this.costPrice = costPrice; }

    public Double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }

    public Double getCurrentMarginPercent() { return currentMarginPercent; }
    public void setCurrentMarginPercent(Double currentMarginPercent) { this.currentMarginPercent = currentMarginPercent; }

    public String getPricingAction() { return pricingAction; }
    public void setPricingAction(String pricingAction) { this.pricingAction = pricingAction; }

    public Double getRecommendedPrice() { return recommendedPrice; }
    public void setRecommendedPrice(Double recommendedPrice) { this.recommendedPrice = recommendedPrice; }

    public Double getProjectedMarginPercent() { return projectedMarginPercent; }
    public void setProjectedMarginPercent(Double projectedMarginPercent) { this.projectedMarginPercent = projectedMarginPercent; }

    public String getPricingReason() { return pricingReason; }
    public void setPricingReason(String pricingReason) { this.pricingReason = pricingReason; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public Double getPosteriorDailyDemand() { return posteriorDailyDemand; }
    public void setPosteriorDailyDemand(Double posteriorDailyDemand) { this.posteriorDailyDemand = posteriorDailyDemand; }

    public Double getDemandCiLow() { return demandCiLow; }
    public void setDemandCiLow(Double demandCiLow) { this.demandCiLow = demandCiLow; }

    public Double getDemandCiHigh() { return demandCiHigh; }
    public void setDemandCiHigh(Double demandCiHigh) { this.demandCiHigh = demandCiHigh; }

    public Double getDaysOfStockLeft() { return daysOfStockLeft; }
    public void setDaysOfStockLeft(Double daysOfStockLeft) { this.daysOfStockLeft = daysOfStockLeft; }

    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }

    public String getInventoryAction() { return inventoryAction; }
    public void setInventoryAction(String inventoryAction) { this.inventoryAction = inventoryAction; }

    public Integer getRecommendedBuyQty() { return recommendedBuyQty; }
    public void setRecommendedBuyQty(Integer recommendedBuyQty) { this.recommendedBuyQty = recommendedBuyQty; }

    public String getInventoryReason() { return inventoryReason; }
    public void setInventoryReason(String inventoryReason) { this.inventoryReason = inventoryReason; }

    public Integer getConfidencePercent() { return confidencePercent; }
    public void setConfidencePercent(Integer confidencePercent) { this.confidencePercent = confidencePercent; }

    public String getDataBasis() { return dataBasis; }
    public void setDataBasis(String dataBasis) { this.dataBasis = dataBasis; }
}
