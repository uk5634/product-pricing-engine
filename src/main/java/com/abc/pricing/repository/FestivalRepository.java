package com.abc.pricing.repository;

import java.time.LocalDate;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.abc.pricing.domain.Festival;

import reactor.core.publisher.Flux;

/*
 * Reactive Repository for Festival entities
 */
@Repository
public interface FestivalRepository extends ReactiveCrudRepository<Festival, String>{

	@Query("SELECT * FROM festivals WHERE enabled = true AND start_date <= :date AND end_date >= :date")
	Flux<Festival> findActiveFestivals(LocalDate date);
	
	Flux<Festival> findByEnabled(boolean enabled);
}
