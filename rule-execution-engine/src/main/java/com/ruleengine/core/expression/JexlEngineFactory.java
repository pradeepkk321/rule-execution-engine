package com.ruleengine.core.expression;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.introspection.JexlSandbox;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating configured JEXL engines.
 * Provides different configurations for different use cases.
 */
public class JexlEngineFactory {
    
    /**
     * Create a default JEXL engine with standard settings.
     * - Caching enabled (512 expressions)
     * - Strict mode (no silent failures)
     * - Utility functions namespace
     */
    public static JexlEngine createDefault() {
        return new JexlBuilder()
                .cache(512)
                .strict(true)
                .silent(false)
                .safe(false)
                .namespaces(createDefaultNamespaces())
                .create();
    }
    
    /**
     * Create a strict JEXL engine.
     * - No silent failures
     * - Strict variable resolution
     * - Fail fast on errors
     */
    public static JexlEngine createStrict() {
        return new JexlBuilder()
                .cache(512)
                .strict(true)
                .silent(false)
                .safe(false)
                .namespaces(createDefaultNamespaces())
                .create();
    }
    
    /**
     * Create a lenient JEXL engine.
     * - Allows undefined variables
     * - Silent failures
     * - Safe navigation (no NPE)
     */
    public static JexlEngine createLenient() {
        return new JexlBuilder()
                .cache(512)
                .strict(false)
                .silent(true)
                .safe(true)
                .namespaces(createDefaultNamespaces())
                .create();
    }
    
    /**
     * Create a sandboxed JEXL engine with restricted permissions.
     * - No access to certain methods/classes
     * - Safe for untrusted expressions
     */
    public static JexlEngine createSandboxed() {
        // Create a sandbox with restricted permissions
        JexlSandbox sandbox = new JexlSandbox(false); // false = whitelist mode
        
        // Allow safe operations
        sandbox.white("java.lang").read("*").execute("*");
        sandbox.white("java.util").read("*").execute("*");
        sandbox.white("java.time").read("*").execute("*");
        
        // Block dangerous operations
        sandbox.black("java.lang.System").read("*").execute("*");
        sandbox.black("java.lang.Runtime").read("*").execute("*");
        sandbox.black("java.lang.ProcessBuilder").read("*").execute("*");
        sandbox.black("java.io").read("*").execute("*");
        sandbox.black("java.nio").read("*").execute("*");
        
        return new JexlBuilder()
                .cache(512)
                .strict(true)
                .silent(false)
                .safe(true)
                .sandbox(sandbox)
                .namespaces(createDefaultNamespaces())
                .create();
    }
    
    /**
     * Create a performance-optimized JEXL engine.
     * - Large cache
     * - Fast execution
     */
    public static JexlEngine createOptimized() {
        return new JexlBuilder()
                .cache(2048)  // Larger cache
                .strict(true)
                .silent(false)
                .safe(false)
                .namespaces(createDefaultNamespaces())
                .create();
    }
    
    /**
     * Create a custom JEXL engine with builder.
     */
    public static JexlBuilder builder() {
        return new JexlBuilder()
                .cache(512)
                .namespaces(createDefaultNamespaces());
    }
    
    /**
     * Create default namespaces with utility functions.
     */
    private static Map<String, Object> createDefaultNamespaces() {
        Map<String, Object> namespaces = new HashMap<>();
        namespaces.put("util", new JexlUtilityFunctions());
        return namespaces;
    }
    
    /**
     * Add custom namespace to existing namespaces.
     */
    public static Map<String, Object> addNamespace(Map<String, Object> namespaces, 
                                                   String name, Object namespace) {
        if (namespaces == null) {
            namespaces = new HashMap<>();
        }
        namespaces.put(name, namespace);
        return namespaces;
    }
}

