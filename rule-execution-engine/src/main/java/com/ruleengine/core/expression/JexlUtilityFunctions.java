package com.ruleengine.core.expression;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility functions available in JEXL expressions.
 * Functions are accessed via the 'util' namespace: util.now(), util.toJson(), etc.
 */
public class JexlUtilityFunctions {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // ====================
    // Date/Time Functions
    // ====================
    
    public Instant now() {
        return Instant.now();
    }
    
    public LocalDate today() {
        return LocalDate.now();
    }
    
    public LocalDateTime currentDateTime() {
        return LocalDateTime.now();
    }
    
    public String formatDate(Object dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        
        if (dateTime instanceof Instant) {
            return formatter.format(((Instant) dateTime).atZone(ZoneId.systemDefault()));
        } else if (dateTime instanceof LocalDateTime) {
            return formatter.format((LocalDateTime) dateTime);
        } else if (dateTime instanceof LocalDate) {
            return formatter.format((LocalDate) dateTime);
        }
        
        return dateTime.toString();
    }
    
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
    
    // ====================
    // Math Functions
    // ====================
    
    /**
     * Round a number to specified decimal places.
     * Usage: util.roundTo(123.456, 2) => 123.46
     */
    public double roundTo(double value, int decimals) {
        if (decimals < 0) {
            throw new IllegalArgumentException("Decimals cannot be negative");
        }
        
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    public double abs(double value) {
        return Math.abs(value);
    }
    
    public long round(double value) {
        return Math.round(value);
    }
    
    public double ceil(double value) {
        return Math.ceil(value);
    }
    
    public double floor(double value) {
        return Math.floor(value);
    }
    
    public double max(double a, double b) {
        return Math.max(a, b);
    }
    
    public double min(double a, double b) {
        return Math.min(a, b);
    }
    
    public double pow(double base, double exponent) {
        return Math.pow(base, exponent);
    }
    
    public double sqrt(double value) {
        return Math.sqrt(value);
    }
    
    // ====================
    // Collection Math Functions
    // ====================
    
    /**
     * Sum items in a cart/list. Each item should have 'price' and 'quantity' fields.
     * Usage: util.sumItems(cartItems)
     */
    public double sumItems(List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        
        double total = 0.0;
        for (Map<String, Object> item : items) {
            Object price = item.get("price");
            Object quantity = item.get("quantity");
            
            if (price != null && quantity != null) {
                double priceValue = toDouble(price);
                double quantityValue = toDouble(quantity);
                total += priceValue * quantityValue;
            }
        }
        
        return total;
    }
    
    /**
     * Sum a specific field from a list of objects.
     * Usage: util.sumField(items, 'total')
     */
    public double sumField(List<Map<String, Object>> items, String field) {
        if (items == null || items.isEmpty() || field == null) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (Map<String, Object> item : items) {
            Object value = item.get(field);
            if (value != null) {
                sum += toDouble(value);
            }
        }
        
        return sum;
    }
    
    /**
     * Calculate average of a field.
     * Usage: util.avgField(items, 'rating')
     */
    public double avgField(List<Map<String, Object>> items, String field) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        
        return sumField(items, field) / items.size();
    }
    
    /**
     * Count items that match a condition.
     * Usage: util.countIf(items, item -> item.price > 100)
     */
    public int countItems(List<?> items) {
        return items != null ? items.size() : 0;
    }
    
    // ====================
    // JSON Functions
    // ====================
    
    public String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to serialize: " + e.getMessage() + "\"}";
        }
    }
    
    public String toPrettyJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to serialize: " + e.getMessage() + "\"}";
        }
    }
    
    public Object fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    // ====================
    // String Functions
    // ====================
    
    public boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    public boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }
    
    public boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public String lower(String str) {
        return str != null ? str.toLowerCase() : null;
    }
    
    public String upper(String str) {
        return str != null ? str.toUpperCase() : null;
    }
    
    public String trim(String str) {
        return str != null ? str.trim() : null;
    }
    
    public boolean contains(String str, String substring) {
        return str != null && substring != null && str.contains(substring);
    }
    
    public boolean startsWith(String str, String prefix) {
        return str != null && prefix != null && str.startsWith(prefix);
    }
    
    public boolean endsWith(String str, String suffix) {
        return str != null && suffix != null && str.endsWith(suffix);
    }
    
    public String substring(String str, int start, int end) {
        if (str == null) return null;
        return str.substring(start, end);
    }
    
    public String replace(String str, String target, String replacement) {
        if (str == null) return null;
        return str.replace(target, replacement);
    }
    
    // ====================
    // Collection Functions
    // ====================
    
    public boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    public boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
    
    public int size(Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }
    
    public boolean contains(Collection<?> collection, Object element) {
        return collection != null && collection.contains(element);
    }
    
    public Object first(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        return collection.iterator().next();
    }
    
    public Object first(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    
    public Object last(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }
    
    // ====================
    // Type Checking & Conversion
    // ====================
    
    public boolean isNull(Object value) {
        return value == null;
    }
    
    public boolean isNotNull(Object value) {
        return value != null;
    }
    
    public Object defaultIfNull(Object value, Object defaultValue) {
        return value != null ? value : defaultValue;
    }
    
    /**
     * Convert various number types to double.
     */
    public double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
    
    /**
     * Convert to integer.
     */
    public int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    // ====================
    // Utility Functions
    // ====================
    
    public String uuid() {
        return UUID.randomUUID().toString();
    }
    
    public int randomInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }
    
    public String join(Collection<?> collection, String delimiter) {
        if (collection == null) {
            return "";
        }
        return collection.stream()
                .map(Object::toString)
                .collect(Collectors.joining(delimiter));
    }
    
    public List<String> split(String str, String delimiter) {
        if (str == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(str.split(delimiter));
    }
    
    /**
     * Coalesce - return first non-null value.
     * Usage: util.coalesce(value1, value2, value3, defaultValue)
     */
    public Object coalesce(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}

