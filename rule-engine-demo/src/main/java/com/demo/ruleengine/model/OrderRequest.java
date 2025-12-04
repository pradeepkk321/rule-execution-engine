package com.demo.ruleengine.model;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

public class OrderRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    @NotEmpty(message = "Items cannot be empty")
    private List<Map<String, Object>> items;
    
    private boolean verified = false;

    // Constructors
    public OrderRequest() {}

    public OrderRequest(String userId, Double amount, List<Map<String, Object>> items, boolean verified) {
        this.userId = userId;
        this.amount = amount;
        this.items = items;
        this.verified = verified;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}