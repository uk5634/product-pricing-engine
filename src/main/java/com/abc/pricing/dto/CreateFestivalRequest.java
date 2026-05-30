package com.abc.pricing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * DTO for creating new festival
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFestivalRequest {

	@NotBlank(message = "Festival name is required")
	private String name;
	
	@NotNull(message = "Start date is required")
	private LocalDate startDate;
	
	@NotNull(message = "End date is required")
	private LocalDate endDate;
	
	@NotNull(message = "Discount percentage is required")
	@Positive(message = "Discount percentage must be positive")
	private BigDecimal discountPercentage;
}
