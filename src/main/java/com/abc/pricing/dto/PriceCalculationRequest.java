package com.abc.pricing.dto;

import java.time.LocalDate;

import com.abc.pricing.domain.MembershipTier;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Request DTO for price calculation with context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationRequest {
	
	@NotBlank(message = "Product Id is required")
	private String productId;
	
	private String userId;
	
	private MembershipTier membershipTier;
	
	private LocalDate date;
	
	private Boolean applyCompetitvePricing;
	
	private Boolean applyInventoryRule;
	
	private Boolean applyLoyaltyDiscount;
	
	private Boolean applyFestivalDiscount;
	
	private String targetCompetitor;


}
