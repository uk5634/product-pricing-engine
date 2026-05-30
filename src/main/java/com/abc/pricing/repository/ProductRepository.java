package com.abc.pricing.repository;

import java.math.BigDecimal;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.abc.pricing.domain.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * Reactive Repository for Product entities
 */
@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, String>{
	
	Flux<Product> findByCategory(String category);
	
	@Query("SELECT * FROM products WHERE inventory_count < :threshold")
	Flux<Product> findLowInventoryProducts(int threshold);
	
	@Query("UPDATE products SET inventory_count = :count, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
	Mono<Void> updateInventory(String id, int count);
	
	@Modifying
	@Query("UPDATE products SET base_price = :price WHERE id = :productId")
	Mono<Long> updateBasePrice(String productId, BigDecimal price);

}
