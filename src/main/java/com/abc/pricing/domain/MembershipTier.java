package com.abc.pricing.domain;

/*
 * Enum representing user membership tiers for loyalty pricing
 */
public enum MembershipTier {

	STANDARD(0.0), GOLD(10.0), PLATINUM(15.0);

	private final double discountPercentage;

	MembershipTier(double discountPercentage) {
		this.discountPercentage = discountPercentage;
	}

	public double getDiscountPercentage() {
		return discountPercentage;
	}

}
