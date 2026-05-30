package com.abc.pricing.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * Entity representing festivals or special occasions for pricing discounts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("festivals")
public class Festival implements Persistable<String>{
	
	@Id
	private String id;
	
	private String name;
	
	@Column("start_date")
	private LocalDate startDate;
	
	@Column("end_date")
	private LocalDate endDate;
	
	@Column("discount_percentage")
	private BigDecimal discountPercentage;
	
	private Boolean enabled;
	
	@Column("created_at")
	private LocalDateTime createdAt;
	
	@Transient
	@Builder.Default
	private boolean isNew = true;
	
	@Override
	public boolean isNew() {
		return isNew;
	}
	
	/*
	 * Check if the festival is active on a given date
	 */
	public boolean isActiveOn(LocalDate date) {
		if(enabled == null || !enabled) {
			return false;
		}
		return !date.isBefore(startDate) && !date.isAfter(endDate);
	}
	
	
}
