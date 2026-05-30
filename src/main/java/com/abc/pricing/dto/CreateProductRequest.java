package com.abc.pricing.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * DTO for creating a new product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
	
	@NotBlank(message = "Product name is required")
	private String name;
	
	private String description;
	
	@NotNull(message = "Base price is required")
	@Positive(message = "Base price must be positive")
	private BigDecimal basePrice;
	
	private String category;
	
	@NotNull(message = "Inventory count is required")
	@Positive(message = "Inventory count must be positive")
	private Integer inventoryCount;
	
	private Integer totalInventory;
	

}
