package com.abc.pricing.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleInfo {
	
	private String name;
	private String ruleType;
	private Integer priority;
	private boolean enabled;
	private boolean dynamic;
	

}
