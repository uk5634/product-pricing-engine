package com.abc.pricing.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.abc.pricing.domain.PricingRuleEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * Reactive Repository for PricingRuleEntity entities
 */
@Repository
public interface PricingRuleRepository extends ReactiveCrudRepository<PricingRuleEntity, String>{

	Flux<PricingRuleEntity> findByEnabled(boolean enabled);
	
	@Query("SELECT * FROM pricing_rules WHERE enabled = true ORDER BY priority ASC")
	Flux<PricingRuleEntity> findAllEnabledOrderByPriority();
	
	Mono<PricingRuleEntity> findByName(String name);
	
	@Query("UPDATE pricing_rules SET enabled = :enabled, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
	Mono<Void> updateEnabled(String id, boolean enabled);
	
}
