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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("audit_history")
public class AuditHistory implements Persistable<String> {

	@Id
	private String id;
	
	private String userId;
	private String username;
	private String membershipTier;
	private String productId;
	private String productName;
	
	@Column("base_price")
	private BigDecimal basePrice;
	private BigDecimal finalPrice;
	private BigDecimal totalDiscount;
	private BigDecimal totalAdjustment;
	
	@Column("created_at")
	private LocalDateTime createdAt;
	
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
}
