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
 * Entity representing a user in the system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User implements Persistable<String>{
	
	@Id
	private String id;
	
	private String username;
	
	private String email;
	
	@Column("membership_tier")
	private MembershipTier membershipTier;
	
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
	 * Check if user is a gold member or higher
	 */
	public boolean isGoldOrHigher() {
		return membershipTier == MembershipTier.GOLD || membershipTier == MembershipTier.PLATINUM;
	}
	
}
