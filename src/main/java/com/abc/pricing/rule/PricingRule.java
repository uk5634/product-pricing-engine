package com.abc.pricing.rule;

import com.abc.pricing.dto.PricingContext;

import reactor.core.publisher.Mono;

/*
 * Interface for all pricing rules.
 * Rules are applied in priority order
 */
public interface PricingRule {
	
	/*
	 * Get the name of the rule.
	 */
	String getName();
	
	/*
	 * Get the type of the rule.
	 */
	String getRuleType();
	
	/*
	 * Get the priority of the rule.
	 */
	int getPriority();
	
	/*
	 * Check if the rule is enabled
	 */
	boolean isEnabled();
	
	/*
	 * Apply the rule to the pricing context
	 * @param context the pricing context containing product , user , and price information
	 * @return updated pricing context with rule applied
	 */
	Mono<PricingContext> apply(PricingContext context);
	
	/*
	 * Check if this rule is application to the given context.
	 */
	boolean isApplicable(PricingContext context);

}
