package com.abc.pricing.rule;

import java.math.BigDecimal;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.abc.pricing.dto.PricingContext;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/*
 * Festival Pricing Rule: Applies additional discounts during festivals.
 */
@Slf4j
@Component
public class FestivalPricingRule extends AbstractPricingRule {

	private final BigDecimal defaultDiscount;
	
	public FestivalPricingRule(
			@Value("${pricing.rules.festival.enabled:true}") boolean enabled,
			@Value("${pricing.rules.festival.priority:400}") int priority, 
			@Value("${pricing.rules.festival.default-discount:15.0}") BigDecimal defaultDiscount
			) {
		super("Festival/Occasion Pricing Rule", "FESTIVAL", priority, enabled);
		this.defaultDiscount = defaultDiscount;
	}
	
	@Override
	public boolean isApplicable(PricingContext context) {
		return context.getActiveFestivals() != null && !context.getActiveFestivals().isEmpty();
	}

	@Override
	protected Mono<PricingContext> applyRule(PricingContext context) {
		return Mono.fromCallable(() -> {
			PricingContext.FestivalInfo bestFestival = context.getActiveFestivals().stream()
					.max(Comparator.comparing(PricingContext.FestivalInfo::getDiscountPercentage))
					.orElse(null);
			if(bestFestival == null) {
				return context;
			}
			BigDecimal discountPercentage = bestFestival.getDiscountPercentage();
			if(discountPercentage == null) {
				discountPercentage = defaultDiscount;
			}
			
			String festivalNames = context.getActiveFestivals().stream()
					.map(PricingContext.FestivalInfo::getName)
					.reduce((a,b) -> a + ", "+b)
					.orElse("Unknown");
			
			String reason = String.format("Festival discount (%s): %s%% off", festivalNames,discountPercentage);
			
			BigDecimal adjustment = calculateAdjustment(context.getCurrentPrice(), discountPercentage).negate();
			BigDecimal newPrice = context.getCurrentPrice().add(adjustment);
			
			BigDecimal minimumPrice = context.getBasePrice().multiply(BigDecimal.valueOf(0.1));
			if(newPrice.compareTo(minimumPrice) < 0) {
				newPrice = minimumPrice;
				adjustment = newPrice.subtract(context.getCurrentPrice());
				reason += " (capped at minimum price)";
			}
			return recordRuleApplication(context, newPrice, adjustment, "PERCENTAGE", reason);
			
		});
		
	}

}
