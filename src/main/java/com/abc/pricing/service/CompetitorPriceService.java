package com.abc.pricing.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abc.pricing.domain.CompetitorPrice;
import com.abc.pricing.repository.CompetitorPriceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitorPriceService {

	@Autowired
	private CompetitorPriceRepository competitorPriceRepository;
	
	@Autowired
	PriceCalculationService priceCalculationService;
	
	/*
	 * Get by all competitor prices for a product
	 */
	public Flux<CompetitorPrice> getCompetitorPrices(String productId) {
		return competitorPriceRepository.findByProductId(productId)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Competitor Price not found: "+ productId)));
	}

	/*
	 * Update or create competitor price
	 */
	public Mono<CompetitorPrice> updateCompetitorPrice(String productId, String competitorName, BigDecimal price) {
		CompetitorPrice existingPrice =  competitorPriceRepository.findByProductIdAndCompetitorName(productId, competitorName).block();
		log.debug("Updating competitor price for {} - {} -> {}",
				productId, competitorName, price);
		Integer returnValue = competitorPriceRepository.upsert(existingPrice != null ? existingPrice.getId(): UUID.randomUUID().toString(), productId, competitorName, price , LocalDateTime.now()).block();
		if(returnValue == 1) {
			priceCalculationService.invalidateCache(productId).subscribe();
		}
		return competitorPriceRepository.findByProductIdAndCompetitorName(productId,competitorName);
	}

}
