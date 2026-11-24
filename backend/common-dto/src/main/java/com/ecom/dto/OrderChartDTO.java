package com.ecom.dto;

public record OrderChartDTO(String month, Long total, Long successful, Double revenue) {
    public OrderChartDTO(String month, Long total, Long successful) {
        this(month, total, successful, 0.0);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String month;
        private Long total;
        private Long successful;
        private Double revenue = 0.0;

        public Builder month(String month) { this.month = month; return this; }
        public Builder total(Long total) { this.total = total; return this; }
        public Builder successful(Long successful) { this.successful = successful; return this; }
        public Builder revenue(Double revenue) { this.revenue = revenue; return this; }
        public OrderChartDTO build() { return new OrderChartDTO(month, total, successful, revenue); }
    }

    public String getMonth() { return month(); }
    public Long getTotal() { return total(); }
    public Long getSuccessful() { return successful(); }
    public Double getRevenue() { return revenue(); }
}
