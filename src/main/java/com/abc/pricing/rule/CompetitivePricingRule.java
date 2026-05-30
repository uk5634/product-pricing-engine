package com.abc.pricing.rule;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.abc.pricing.dto.PricingContext;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/*
 * Competitive Pricing Rule, Adjusts price to be within +/- 3% of average competitor price.
 * Priority: 100 applied first.
 */
@Slf4j
@Component
public class CompetitivePricingRule extends AbstractPricingRule {

	private final BigDecimal adjustmentPercentage;
	
	public CompetitivePricingRule(
			@Value("${pricing.rules.competitive.enabled:true}") boolean enabled,
			@Value("${pricing.rules.competitive.priority:100}") int priority,
			@Value("${pricing.rules.competitive.adjustment-percentage:3.0}") BigDecimal adjustmentPercentage
			) {
		super("Competitive Pricing Rule", "COMPETITIVE", priority, enabled);
		this.adjustmentPercentage = adjustmentPercentage;
	}
	
	
	@Override
	public boolean isApplicable(PricingContext context) {
		return context.getAverageCompetitorPrice() != null && context.getAverageCompetitorPrice().compareTo(BigDecimal.ZERO) > 0;
	}

	@Override
	protected Mono<PricingContext> applyRule(PricingContext context) {
		return Mono.fromCallable(() ->{
			BigDecimal currentPrice = context.getCurrentPrice();
			BigDecimal avgCompetitorPrice = context.getAverageCompetitorPrice();
			BigDecimal lowestCompetitorPrice = context.getLowestCompetitorPrice();
			
			BigDecimal lowerBound = avgCompetitorPrice.multiply(
					BigDecimal.ONE.subtract(
							adjustmentPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
			
			BigDecimal upperBound = avgCompetitorPrice.multiply(
					BigDecimal.ONE.add(
							adjustmentPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
			
			BigDecimal newPrice = currentPrice;
			String reason;
			
			if(currentPrice.compareTo(upperBound) > 0) {
				newPrice = upperBound.setScale(2, RoundingMode.HALF_UP);
				reason = String.format("Price reduced to stay within +%s%% of competitor average (%.2f)", adjustmentPercentage, avgCompetitorPrice);
			} else if( currentPrice.compareTo(lowerBound) < 0) {
				reason = String.format("Price already below competitor range (avg: %.2f, lowest: %.2f)", avgCompetitorPrice, lowestCompetitorPrice);
			} else {
				reason = String.format("Price within competitive range [%.2f - %.2f]", lowerBound, upperBound);
			}
			BigDecimal adjustment = newPrice.subtract(currentPrice);
			return recordRuleApplication(context, newPrice, adjustment, "PERCENTAGE", reason);
		});
	}
}
