package com.abc.pricing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Response DTO containing the calculated price and breakdown
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponse {
	
	private String productId;
	private String productName;
	private BigDecimal basePrice;
	private BigDecimal finalPrice;
	private BigDecimal totalDiscount;
	private BigDecimal totalAdjustment;
	private List<RuleApplication> appliedRules;
	private LocalDateTime calculatedAt;
	private String cacheStatus;
	
	

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RuleApplication{
		private String ruleName;
		private String ruleType;
		private int priority;
		private BigDecimal priceBeforeRule;
		private BigDecimal priceAfterRule;
		private BigDecimal adjustment;
		private String adjustmentType;
		private String reason;
		
	}
	

	
	

}
