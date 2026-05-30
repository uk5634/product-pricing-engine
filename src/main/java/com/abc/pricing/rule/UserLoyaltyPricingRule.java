package com.abc.pricing.rule;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.abc.pricing.domain.MembershipTier;
import com.abc.pricing.dto.PricingContext;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/*
 * User Loyalty Rule: Provides discount based on membership tier.
 * Gold members get 10% discount.
 * Platinum members get 15% discount.
 */
@Slf4j
@Component
public class UserLoyaltyPricingRule extends AbstractPricingRule {
	
	private final BigDecimal goldMemberDiscount;
	
	public UserLoyaltyPricingRule(
			@Value("${pricing.rules.loyalty.enabled:true}") boolean enabled,
			@Value("${ricing.rules.loyalty.priority:300}") int priority,
			@Value("${pricing.rules.loyalty.gold-member-discount:10.0}") BigDecimal goldMemberDiscount,
			@Value("${pricing.rules.loyalty.platinum-member-discount:15.0}") BigDecimal platinumMemberDiscount
			) {
		super("User Loyalty Rule", "LOYALTY", priority, enabled);
		this.goldMemberDiscount = goldMemberDiscount;
	}
	
	@Override
	public boolean isApplicable(PricingContext context) {
		return context.getMembershipTier() != null && context.getMembershipTier() != MembershipTier.STANDARD;
	}

	@Override
	protected Mono<PricingContext> applyRule(PricingContext context) {
		return Mono.fromCallable(() -> {
			MembershipTier tier = context.getMembershipTier();
			BigDecimal discountPercentage;
			String reason;
			
			switch(tier) {
			case PLATINUM:
				discountPercentage = BigDecimal.valueOf(tier.getDiscountPercentage());
				reason = String.format("Platinum member discount: %s%%", discountPercentage);
				break;
			case GOLD:
				discountPercentage = goldMemberDiscount;
				reason = String.format("Gold member discount: %s%%", discountPercentage);
				break;
			default:
				return context;
			}
			
			BigDecimal adjustment = calculateAdjustment(context.getCurrentPrice(), discountPercentage).negate();
			BigDecimal newPrice = context.getCurrentPrice().add(adjustment);
			return recordRuleApplication(context, newPrice, adjustment, "PERCENTAGE", reason);
		});
		
		
		
		
		
		
		
		
		
		
		
		
		
	}

}
