package com.omnicart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderChartData {
    private String month;
    private Long total;
    private Long successful;

    public OrderChartData() {}

    public OrderChartData(String month, Long total, Long successful) {
        this.month = month;
        this.total = total;
        this.successful = successful;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public Long getTotal() { return total != null ? total : 0L; }
    public void setTotal(Long total) { this.total = total; }

    public Long getSuccessful() { return successful != null ? successful : 0L; }
    public void setSuccessful(Long successful) { this.successful = successful; }
}
