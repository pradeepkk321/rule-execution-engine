package com.ruleengine.core.expression;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    
    /**
     * Get current timestamp as Instant.
     * Usage: util.now()
     */
    public Instant now() {
        return Instant.now();
    }
    
    /**
     * Get current date.
     * Usage: util.today()
     */
    public LocalDate today() {
        return LocalDate.now();
    }
    
    /**
     * Get current date-time.
     * Usage: util.currentDateTime()
     */
    public LocalDateTime currentDateTime() {
        return LocalDateTime.now();
    }
    
    /**
     * Format a date/time object.
     * Usage: util.formatDate(dateObject, 'yyyy-MM-dd')
     */
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
    
    /**
     * Get current timestamp in milliseconds.
     * Usage: util.currentTimeMillis()
     */
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
    
    // ====================
    // JSON Functions
    // ====================
    
    /**
     * Convert object to JSON string.
     * Usage: util.toJson(object)
     */
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
    
    /**
     * Convert object to pretty-printed JSON string.
     * Usage: util:toPrettyJson(object)
     */
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
    
    /**
     * Parse JSON string to object.
     * Usage: util:fromJson(jsonString)
     */
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
    
    /**
     * Check if string is empty or null.
     * Usage: util:isEmpty(str)
     */
    public boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * Check if string is not empty.
     * Usage: util:isNotEmpty(str)
     */
    public boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }
    
    /**
     * Check if string is blank (empty or only whitespace).
     * Usage: util:isBlank(str)
     */
    public boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Convert string to lowercase.
     * Usage: util:lower(str)
     */
    public String lower(String str) {
        return str != null ? str.toLowerCase() : null;
    }
    
    /**
     * Convert string to uppercase.
     * Usage: util:upper(str)
     */
    public String upper(String str) {
        return str != null ? str.toUpperCase() : null;
    }
    
    /**
     * Trim whitespace from string.
     * Usage: util:trim(str)
     */
    public String trim(String str) {
        return str != null ? str.trim() : null;
    }
    
    /**
     * Check if string contains substring.
     * Usage: util:contains(str, substring)
     */
    public boolean contains(String str, String substring) {
        return str != null && substring != null && str.contains(substring);
    }
    
    /**
     * Check if string starts with prefix.
     * Usage: util:startsWith(str, prefix)
     */
    public boolean startsWith(String str, String prefix) {
        return str != null && prefix != null && str.startsWith(prefix);
    }
    
    /**
     * Check if string ends with suffix.
     * Usage: util:endsWith(str, suffix)
     */
    public boolean endsWith(String str, String suffix) {
        return str != null && suffix != null && str.endsWith(suffix);
    }
    
    // ====================
    // Collection Functions
    // ====================
    
    /**
     * Check if collection is empty or null.
     * Usage: util:isEmpty(collection)
     */
    public boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * Check if collection is not empty.
     * Usage: util:isNotEmpty(collection)
     */
    public boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
    
    /**
     * Get size of collection.
     * Usage: util:size(collection)
     */
    public int size(Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }
    
    /**
     * Check if collection contains element.
     * Usage: util:contains(collection, element)
     */
    public boolean contains(Collection<?> collection, Object element) {
        return collection != null && collection.contains(element);
    }
    
    /**
     * Get first element of collection.
     * Usage: util:first(collection)
     */
    public Object first(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        return collection.iterator().next();
    }
    
    /**
     * Get first element of list.
     * Usage: util:first(list)
     */
    public Object first(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    
    /**
     * Get last element of list.
     * Usage: util:last(list)
     */
    public Object last(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }
    
    // ====================
    // Math Functions
    // ====================
    
    /**
     * Get absolute value.
     * Usage: util:abs(number)
     */
    public double abs(double value) {
        return Math.abs(value);
    }
    
    /**
     * Round to nearest integer.
     * Usage: util:round(number)
     */
    public long round(double value) {
        return Math.round(value);
    }
    
    /**
     * Round up (ceiling).
     * Usage: util:ceil(number)
     */
    public double ceil(double value) {
        return Math.ceil(value);
    }
    
    /**
     * Round down (floor).
     * Usage: util:floor(number)
     */
    public double floor(double value) {
        return Math.floor(value);
    }
    
    /**
     * Get maximum of two numbers.
     * Usage: util:max(a, b)
     */
    public double max(double a, double b) {
        return Math.max(a, b);
    }
    
    /**
     * Get minimum of two numbers.
     * Usage: util:min(a, b)
     */
    public double min(double a, double b) {
        return Math.min(a, b);
    }
    
    // ====================
    // Type Checking Functions
    // ====================
    
    /**
     * Check if value is null.
     * Usage: util:isNull(value)
     */
    public boolean isNull(Object value) {
        return value == null;
    }
    
    /**
     * Check if value is not null.
     * Usage: util:isNotNull(value)
     */
    public boolean isNotNull(Object value) {
        return value != null;
    }
    
    /**
     * Get default value if value is null.
     * Usage: util:defaultIfNull(value, defaultValue)
     */
    public Object defaultIfNull(Object value, Object defaultValue) {
        return value != null ? value : defaultValue;
    }
    
    // ====================
    // Utility Functions
    // ====================
    
    /**
     * Generate a random UUID.
     * Usage: util:uuid()
     */
    public String uuid() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Generate a random integer between min and max (inclusive).
     * Usage: util:randomInt(min, max)
     */
    public int randomInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }
    
    /**
     * Join collection elements into a string.
     * Usage: util:join(collection, delimiter)
     */
    public String join(Collection<?> collection, String delimiter) {
        if (collection == null) {
            return "";
        }
        return collection.stream()
                .map(Object::toString)
                .collect(Collectors.joining(delimiter));
    }
    
    /**
     * Split string into list.
     * Usage: util:split(str, delimiter)
     */
    public List<String> split(String str, String delimiter) {
        if (str == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(str.split(delimiter));
    }
}