package com.demo.ruleengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = {"com.demo.ruleengine", "com.ruleengine.spring.boot"})
public class RuleEngineDemoApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RuleEngineDemoApplication.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}