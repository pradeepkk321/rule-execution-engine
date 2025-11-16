package com.ruleengine.spring.boot;

import com.ruleengine.core.action.ActionProvider;
import com.ruleengine.core.action.ActionRegistry;
import com.ruleengine.core.action.builtin.ScriptActionProvider;
import com.ruleengine.core.config.ConfigurationLoader;
import com.ruleengine.core.executor.RuleEngineBuilder;
import com.ruleengine.core.executor.RuleExecutor;
import com.ruleengine.core.expression.ExpressionEvaluator;
import com.ruleengine.core.expression.JexlExpressionEvaluator;
import com.ruleengine.core.model.RuleEngineConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Spring Boot auto-configuration for the rule engine.
 * 
 * This configuration:
 * - Loads rule configuration from application.yml
 * - Creates and configures RuleExecutor bean
 * - Registers all ActionProvider beans
 * - Provides sensible defaults
 */
@Configuration
@ConditionalOnClass(RuleExecutor.class)
@EnableConfigurationProperties(RuleEngineProperties.class)
public class RuleEngineAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleEngineAutoConfiguration.class);
    
    /**
     * Create ConfigurationLoader bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public ConfigurationLoader configurationLoader() {
        return new ConfigurationLoader();
    }
    
    /**
     * Create ExpressionEvaluator bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public ExpressionEvaluator expressionEvaluator() {
        logger.info("Creating default JEXL expression evaluator");
        return new JexlExpressionEvaluator();
    }
    
    /**
     * Create ActionRegistry and register all ActionProvider beans.
     */
    @Bean
    @ConditionalOnMissingBean
    public ActionRegistry actionRegistry(ExpressionEvaluator expressionEvaluator,
                                        List<ActionProvider> actionProviders) {
        
        logger.info("Creating action registry");
        ActionRegistry registry = new ActionRegistry();
        
        // Register built-in ScriptAction
        logger.info("Registering built-in ScriptActionProvider");
        registry.registerProvider(new ScriptActionProvider(expressionEvaluator));
        
        // Register all custom ActionProvider beans
        if (actionProviders != null && !actionProviders.isEmpty()) {
            logger.info("Registering {} custom action providers", actionProviders.size());
            actionProviders.forEach(provider -> {
                logger.info("  - Registering: {}", provider.getProviderName());
                registry.registerProvider(provider);
            });
        }
        
        return registry;
    }
    
    /**
     * Load RuleEngineConfig from configured location.
     */
    @Bean
    @ConditionalOnMissingBean
    public RuleEngineConfig ruleEngineConfig(
            RuleEngineProperties properties,
            ConfigurationLoader loader,
            ResourceLoader resourceLoader) throws Exception {
        
        String location = properties.getConfigLocation();
        logger.info("Loading rule engine configuration from: {}", location);
        
        try {
            Resource resource = resourceLoader.getResource(location);
            
            if (!resource.exists()) {
                throw new IllegalStateException(
                    "Rule configuration not found at: " + location);
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                RuleEngineConfig config = loader.loadFromInputStream(inputStream, location);
                
                // Override global settings from properties if specified
                if (properties.getMaxExecutionDepth() != null) {
                    config.getGlobalSettings().setMaxExecutionDepth(
                        properties.getMaxExecutionDepth());
                }
                if (properties.getExecutionTimeout() != null) {
                    config.getGlobalSettings().setTimeout(
                        properties.getExecutionTimeout());
                }
                
                logger.info("Successfully loaded configuration: {} rules", 
                           config.getRuleCount());
                
                return config;
            }
            
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to load rule configuration from: " + location, e);
        }
    }
    
    /**
     * Create RuleExecutor bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public RuleExecutor ruleExecutor(
            RuleEngineConfig config,
            ActionRegistry actionRegistry,
            ExpressionEvaluator expressionEvaluator,
            RuleEngineProperties properties) throws Exception {
        
        logger.info("Creating RuleExecutor");
        logger.info("Properties: {}", properties);
        
        RuleExecutor executor = RuleEngineBuilder.create()
                .withConfig(config)
                .withActionRegistry(actionRegistry)
                .withExpressionEvaluator(expressionEvaluator)
                .withValidation(properties.isValidateOnStartup())
                .build();
        
        logger.info("RuleExecutor created successfully");
        
        return executor;
    }
    
    /**
     * Create RuleEngineService bean for easier usage.
     */
    @Bean
    @ConditionalOnMissingBean
    public RuleEngineService ruleEngineService(RuleExecutor ruleExecutor) {
        return new RuleEngineService(ruleExecutor);
    }
}