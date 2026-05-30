package com.abc.pricing.rule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import com.abc.pricing.dto.PriceResponse;
import com.abc.pricing.dto.PricingContext;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/*
 * Abstract base class for pricing rules providing common functionality.
 */
@Slf4j
public abstract class AbstractPricingRule implements PricingRule {

	protected final String name;
	protected final String ruleType;
	protected final int priority;
	protected boolean enabled;
	
	public AbstractPricingRule(String name, String ruleType, int priority, boolean enabled) {
		this.name = name;
		this.ruleType = ruleType;
		this.priority = priority;
		this.enabled = enabled;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getRuleType() {
		return ruleType;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/*
	 * Apply a percentage adjustment to the current price.
	 * Positive percentage increases price, negative decreases.
	 */
	protected BigDecimal applyPercentageAdjustment(BigDecimal currentPrice, BigDecimal percentage) {
		BigDecimal adjustment = currentPrice.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		return currentPrice.add(adjustment).setScale(2, RoundingMode.HALF_UP);
	}
	
	/*
	 * Calculate the adjustment amount for a given percentage
	 */
	protected BigDecimal calculateAdjustment(BigDecimal currentPrice, BigDecimal percentage) {
		return currentPrice.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
	}
	
	/*
	 * Record the rule application in the context.
	 */
	protected PricingContext recordRuleApplication(PricingContext context, BigDecimal priceAfter, BigDecimal adjustment, String adjustmentType, String reason) {
		if(context.getAppliedRules() == null) {
			context.setAppliedRules(new ArrayList<>());
		}
		PriceResponse.RuleApplication ruleApplication = PriceResponse.RuleApplication.builder()
				.ruleName(name)
				.ruleType(ruleType)
				.priority(priority)
				.priceBeforeRule(context.getCurrentPrice())
				.priceAfterRule(priceAfter)
				.adjustment(adjustment)
				.adjustmentType(adjustmentType)
				.reason(reason)
				.build();
		context.getAppliedRules().add(ruleApplication);
		context.setCurrentPrice(priceAfter);
		log.info("Rule '{}' applied: {} -> {} (adjustment: {}, reason: {})", name, ruleApplication.getPriceBeforeRule(), priceAfter, adjustment, reason);
		return context;
	}
	
	@Override
	public Mono<PricingContext> apply(PricingContext context) {
		if(!isEnabled() || !isApplicable(context)) {
			log.info("Rule '{}' skipped - enabled: {}, applicable: {}", name, isEnabled(), isApplicable(context));
			return Mono.just(context);
		}
		return applyRule(context);
	}

	/*
	 * Template method for subclasses to implement the actual rule logic.
	 */
	protected abstract Mono<PricingContext> applyRule(PricingContext context);
	

}
