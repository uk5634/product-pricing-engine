package com.abc.pricing.domain;

import java.math.BigDecimal;
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
 * Entity Representing Competitor Pricing Data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("competitor_prices")
public class CompetitorPrice implements Persistable<String>{

	@Id
	private String id;
	
	@Column("product_id")
	private String productId;
	
	@Column("competitor_name")
	private String competitorName;
	
	private BigDecimal price;
	
	@Column("last_updated")
	private LocalDateTime lastUpdated;
	
	@Transient
	@Builder.Default
	private boolean isNew = true;
	
	@Override
	public boolean isNew() {
		return isNew;
	}
	
}
