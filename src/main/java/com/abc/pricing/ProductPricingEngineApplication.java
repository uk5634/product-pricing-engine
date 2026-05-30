package com.abc.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Reactive Pricing Engine.
 * This microservice computes product price in real-time using a pipeline of configurable rules.
 */
@SpringBootApplication
@EnableScheduling
public class ProductPricingEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductPricingEngineApplication.class, args);
	}

}
