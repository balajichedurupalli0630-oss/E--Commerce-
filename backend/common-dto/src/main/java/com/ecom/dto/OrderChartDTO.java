package com.ecom.dto;

public class OrderChartDTO {
    private String month;
    private Long total;
    private Long successful;

    public OrderChartDTO() {}

    public OrderChartDTO(String month, Long total, Long successful) {
        this.month = month;
        this.total = total;
        this.successful = successful;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String month;
        private Long total;
        private Long successful;

        public Builder month(String month) { this.month = month; return this; }
        public Builder total(Long total) { this.total = total; return this; }
        public Builder successful(Long successful) { this.successful = successful; return this; }

        public OrderChartDTO build() {
            return new OrderChartDTO(month, total, successful);
        }
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    public Long getSuccessful() { return successful; }
    public void setSuccessful(Long successful) { this.successful = successful; }
}
