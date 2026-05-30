package com.abc.pricing.rule;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.abc.pricing.dto.PricingContext;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/*
 * Inventory Rule: Increases price by 10% when inventory is low.
 * Low inventory is - less then 100 units or less then 5 % of total inventory.
 */
@Slf4j
@Component
public class InventoryPricingRule extends AbstractPricingRule {

	private final int lowStockThreshold;
	private final BigDecimal lowStockPercentage;
	private final BigDecimal priceIncreasePercentage;
	
	public InventoryPricingRule(
			@Value("${pricing.rules.inventory.enabled:true}") boolean enabled,
			@Value("${pricing.rules.inventory.priority:200}") int priority,
			@Value("${pricing.rules.inventory.low-stock-threshold:100}") int lowStockThreshold,
			@Value("${pricing.rules.inventory.low-stock-percentage:5.0}") BigDecimal lowStockPercentage,
			@Value("${pricing.rules.inventory.price-increase-percentage:10.0}") BigDecimal priceIncreasePercentage
			) {
		super("Inventory Pricing Rule","INVENTORY",priority, enabled);
		this.lowStockThreshold = lowStockThreshold;
		this.lowStockPercentage = lowStockPercentage;
		this.priceIncreasePercentage = priceIncreasePercentage;
	}
	
	@Override
	public boolean isApplicable(PricingContext context) {
		return context.getInventoryCount() != null;
	}

	@Override
	protected Mono<PricingContext> applyRule(PricingContext context) {
		return Mono.fromCallable(() -> {
			Integer inventoryCount = context.getInventoryCount();
			Integer totalInventory = context.getTotalInventory();
			
			boolean isLowStock = false;
			String reason;
			
			if(inventoryCount < lowStockThreshold) {
				isLowStock = true;
				reason = String.format("Low stock: %d units (threshold: %d)", inventoryCount, lowStockThreshold);
			} else if (totalInventory != null && totalInventory > 0) {
				double inventoryPercentage = (inventoryCount * 100.0) / totalInventory;
				if(inventoryPercentage < lowStockPercentage.doubleValue()) {
					isLowStock = true;
					reason = String.format("Low stock: %.1f%% remaining (threshold: %s%%)", inventoryPercentage, lowStockPercentage);
				} else {
					reason = String.format("Adequate stock: %d units (%.1f%%)", inventoryCount, inventoryPercentage);
				}
			} else {
				reason = String.format("Adequate stock: %d units", inventoryCount);
			}
			if(isLowStock) {
				BigDecimal newPrice = applyPercentageAdjustment(context.getCurrentPrice(), priceIncreasePercentage);
				BigDecimal adjustment = calculateAdjustment(context.getCurrentPrice(),priceIncreasePercentage);
				return recordRuleApplication(context, newPrice, adjustment, "PERCENTAGE", reason + " - Price increased by " + priceIncreasePercentage + "%");
			}
			return recordRuleApplication(context, context.getCurrentPrice(), BigDecimal.ZERO, "NONE", reason + " - No price adjustment");
		});
	}

}
