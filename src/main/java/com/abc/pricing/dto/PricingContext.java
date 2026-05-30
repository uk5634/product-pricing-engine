package com.abc.pricing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.abc.pricing.domain.MembershipTier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Context object containing all data needed for price calculation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingContext {

	private String productId;
	private String productName;
	private String category;
	private BigDecimal basePrice;
	private BigDecimal currentPrice;
	
	private Integer inventoryCount;
	private Integer totalInventory;
	
	private String userId;
	private MembershipTier membershipTier;
	
	private List<CompetitorPriceInfo> competitorPrices;
	private BigDecimal averageCompetitorPrice;
	private BigDecimal lowestCompetitorPrice;
	
	private LocalDate calculationDate;
	private List<FestivalInfo> activeFestivals;
	
	private List<PriceResponse.RuleApplication> appliedRules;
	
	
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CompetitorPriceInfo{
		private String competitorName;
		private BigDecimal price;
	}
	
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FestivalInfo{
		private String name;
		private BigDecimal discountPercentage;
	}
	
}
