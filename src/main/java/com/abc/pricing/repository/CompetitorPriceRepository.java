package com.abc.pricing.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.abc.pricing.domain.CompetitorPrice;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * Reactive repository for competitor price entities
 */
@Repository
public interface CompetitorPriceRepository extends ReactiveCrudRepository<CompetitorPrice, String>{

	Flux<CompetitorPrice> findByProductId(String productId);
	
	Mono<CompetitorPrice> findByProductIdAndCompetitorName(String productId, String competitorName);
	
	@Query("SELECT AVG(price) FROM competitor_prices WHERE product_id = :productId")
	Mono<BigDecimal> findAveragePrice(String productId);
	
	@Query("SELECT MIN(price) FROM competitor_prices WHERE product_id = :productId")
	Mono<BigDecimal> findLowestPrice(String productId);
	
	@Modifying
	@Query("MERGE INTO competitor_prices (ID, product_id, competitor_name, PRICE, last_updated) KEY(ID) VALUES (:id, :productId, :competitorName, :price, :lastUpdated)")
	Mono<Integer> upsert(String id, String productId, String competitorName, BigDecimal price, LocalDateTime lastUpdated);

}
