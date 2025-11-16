package com.demo.ruleengine.model;

public class OrderResponse {
    private boolean success;
    private String orderId;
    private Double total;
    private String status;
    private String message;
    private String error;
    private Long executionTimeMs;

    // Private constructor for builder
    private OrderResponse(Builder builder) {
        this.success = builder.success;
        this.orderId = builder.orderId;
        this.total = builder.total;
        this.status = builder.status;
        this.message = builder.message;
        this.error = builder.error;
        this.executionTimeMs = builder.executionTimeMs;
    }

    // Static builder method
    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getOrderId() {
        return orderId;
    }

    public Double getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    // Builder class
    public static class Builder {
        private boolean success;
        private String orderId;
        private Double total;
        private String status;
        private String message;
        private String error;
        private Long executionTimeMs;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder total(Double total) {
            this.total = total;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder executionTimeMs(Long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public OrderResponse build() {
            return new OrderResponse(this);
        }
    }
}