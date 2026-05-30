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
 * Product entity representing items in the e commerce system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("products")
public class Product implements Persistable<String>{
	
	@Id
	private String id;
	
	private String name;
	
	private String description;
	
	@Column("base_price")
	private BigDecimal basePrice;
	
	private String category;
	
	@Column("inventory_count")
	private Integer inventoryCount;
	
	@Column("total_inventory")
	private Integer totalInventory;
	
	@Column("created_at")
	private LocalDateTime createdAt;
	
	@Column("updated_at")
	private LocalDateTime updatedAt;
	
	@Transient
	@Builder.Default
	private boolean isNew = true;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        // If true, Spring performs INSERT; if false, Spring performs UPDATE
        return isNew || id == null;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    
	/*
	 * Calculate the inventory percentage remaining
	 */
	public double getInventoryPercentage() {
		if (totalInventory == null || totalInventory == 0) {
			return 100.0;
		}
		return (inventoryCount * 100.0) / totalInventory;
	}
	
	/*
	 * Check if inventory is low below threshold
	 */
	public boolean isLowInventory(int threshold, double percentageThreshold) {
		return inventoryCount < threshold || getInventoryPercentage() < percentageThreshold;
	}

}
