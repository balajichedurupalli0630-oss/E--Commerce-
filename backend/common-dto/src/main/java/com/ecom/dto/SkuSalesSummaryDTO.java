package com.ecom.dto;

public record SkuSalesSummaryDTO(Long productId, Integer unitsSold, Integer windowDays) {
    public Long getProductId() { return productId(); }
    public Integer getUnitsSold() { return unitsSold(); }
    public Integer getWindowDays() { return windowDays(); }
    public Integer getTotalUnitsSold() { return unitsSold(); }
    public Integer getObservationDays() { return windowDays(); }
}
