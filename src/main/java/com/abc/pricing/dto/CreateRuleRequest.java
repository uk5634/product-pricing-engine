package com.abc.pricing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * DTO for creating a dynamic pricing rule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRuleRequest {

	@NotBlank(message = "Rule name is required")
	private String name;
	
	private String description;
	
	@NotBlank(message = "Rule type is required")
	private String ruleType;
	
	@NotNull(message = "Priority is required")
	@Positive(message = "Priority must be positive")
	private Integer priority;
	
	/*
	 * JSON configuration for the rule
	 * Ex - {"discountPercentage": 10, "condition": "category =='electronics'"}
	 */
	@NotBlank(message = "Configuration is required")
	private String configuration;
	
}

