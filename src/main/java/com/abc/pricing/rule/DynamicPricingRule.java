package com.abc.pricing.rule;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.abc.pricing.dto.PricingContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/*
 * Dynamic Pricing Rule: A configurable rule loaded from database.
 * Supports various rule types through JSON configuration.
 */
@Slf4j
public class DynamicPricingRule extends AbstractPricingRule {

	private final String id;
	private final String configuration;
	private final ObjectMapper objectMapper;
	private JsonNode configNode;
	
	public DynamicPricingRule(String id, String name, String ruleType, int priority, boolean enabled, String configuration) {
		super(name, ruleType, priority, enabled);
		this.id = id;
		this.configuration = configuration;
		this.objectMapper = new ObjectMapper();
		parseConfiguration();
	}

	private void parseConfiguration() {
		try {
			this.configNode = objectMapper.readTree(configuration);
		} catch(Exception e) {
			log.error("Failed to parse rule configuration: {}", configuration, e);
			this.configNode = objectMapper.createObjectNode();
		}
	}

	@Override
	public boolean isApplicable(PricingContext context) {
		if(configNode.has("condition")) {
			return evaluateCondition(context, configNode.get("condition").asText());
		}
		if(configNode.has("category")) {
			return evaluateCondition(context, configNode.get("category").asText());
		}
		if(configNode.has("productId")) {
			return evaluateCondition(context, configNode.get("productId").asText());
		}
		return true;
	}

	private boolean evaluateCondition(PricingContext context, String condition) {
		try {
			if(condition.contains("category ==")) {
				String categoryValue = extractStringValue(condition);
				return categoryValue.equalsIgnoreCase(context.getCategory());
			}
			if(condition.contains("inventoryCount <")) {
				int threshold = extractIntValue(condition);
				return context.getInventoryCount() != null && context.getInventoryCount() < threshold;
			}
			if(condition.contains("inventoryCount >")) {
				int threshold = extractIntValue(condition);
				return context.getInventoryCount() != null && context.getInventoryCount() > threshold;
			}
			if(condition.contains("basePrice >")) {
				BigDecimal threshold = extractDecimalValue(condition);
				return context.getBasePrice().compareTo(threshold) > 0;
			}
			if(condition.contains("basePrice <")) {
				BigDecimal threshold = extractDecimalValue(condition);
				return context.getBasePrice().compareTo(threshold) < 0;
			}
		} catch(Exception e) {}
		return true;
	}

	private String extractStringValue(String condition) {
		int start = condition.indexOf("'") + 1;
		int end = condition.lastIndexOf("'");
		return condition.substring(start, end);
	}
	
	private int extractIntValue(String condition) {
		String[] parts = condition.split("[<>]=?");
		return Integer.parseInt(parts[1].trim());
	}
	
	private BigDecimal extractDecimalValue(String condition) {
		String[] parts = condition.split("[<>]=?");
		return new BigDecimal(parts[1].trim());
	}

	@Override
	protected Mono<PricingContext> applyRule(PricingContext context) {
		// TODO Auto-generated method stub
		return Mono.fromCallable(() -> {
			String reason = configNode.has("reason") ?
					configNode.get("reason").asText() : "Dynamic rule: "+ getName();
			
			if(configNode.has("discountPercentage") ) {
				BigDecimal discountPercentage = new BigDecimal(configNode.get("discountPercentage").asText());
				BigDecimal adjustment = calculateAdjustment(context.getCurrentPrice(),discountPercentage).negate();
				BigDecimal newPrice = context.getCurrentPrice().add(adjustment);
				return recordRuleApplication(context, newPrice, adjustment, "PERCENTAGE", reason);
			}
			if(configNode.has("increasePercentage") ) {
				BigDecimal increasePercentage = new BigDecimal(configNode.get("increasePercentage").asText());
				BigDecimal newPrice = applyPercentageAdjustment(context.getCurrentPrice(), increasePercentage);
				BigDecimal adjustment = newPrice.subtract(context.getCurrentPrice());
				return recordRuleApplication(context, newPrice, adjustment, "PERCENTAGE", reason);
			}
			if(configNode.has("discountAmount") ) {
				BigDecimal discountAmount = new BigDecimal(configNode.get("discountAmount").asText());
				BigDecimal newPrice = context.getCurrentPrice().subtract(discountAmount);
				if(newPrice.compareTo(BigDecimal.ZERO) < 0) {
					newPrice = BigDecimal.ZERO;
				}
				return recordRuleApplication(context, newPrice, discountAmount.negate(), "ABSOLUTE", reason);
			}
			if(configNode.has("fixedPrice") ) {
				BigDecimal fixedPrice = new BigDecimal(configNode.get("fixedPrice").asText());
				BigDecimal adjustment = fixedPrice.subtract(context.getCurrentPrice());
				return recordRuleApplication(context, fixedPrice, adjustment, "FIXED", reason);
			}
			return context;
		});
		
	}	
}
