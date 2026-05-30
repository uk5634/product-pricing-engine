package com.abc.pricing.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitorPriceMessage {
	
	private String productId;
	
	private String productName;
	
	private String competitorName;
	
	private String competitorUrl;
	
	private BigDecimal price;
	
	private String source;
	
	@Builder.Default
	private String currency = "INR";
		

}
