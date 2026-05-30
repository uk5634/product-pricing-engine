package com.abc.pricing.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Entity representing a dynamic pricing rule stored in the database
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("pricing_rules")
public class PricingRuleEntity implements Persistable<String>{

	@Id
	private String id;
	
	private String name;
	
	private String description;
	
	@Column("rule_type")
	private String ruleType;
	
	private Integer priority;
	
	private Boolean enabled;
	
	/*
	 * JSON configuration for the rule
	 * Ex - {"discountPercentage": 10, "condition": "category =='electronics'"}
	 */
	private String configuration;
	
	@Column("created_at")
	private LocalDateTime createdAt;
	
	@Column("updated_at")
	private LocalDateTime updatedAt;
	
	@Transient
	@Builder.Default
	private boolean isNew = true;
	
	@Override
	public boolean isNew() {
		return isNew;
	}
}
