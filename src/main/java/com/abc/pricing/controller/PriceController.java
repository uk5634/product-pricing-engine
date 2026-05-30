package com.abc.pricing.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abc.pricing.domain.MembershipTier;
import com.abc.pricing.dto.PriceCalculationRequest;
import com.abc.pricing.dto.PriceResponse;
import com.abc.pricing.service.PriceCalculationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * Rest controller for price calculations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
public class PriceController {

	@Autowired
	private PriceCalculationService priceCalculationService;
	
	
	@GetMapping("/{productId}")
	public Mono<PriceResponse> getPrice(@PathVariable String productId, @RequestParam(required = false) String userId,
			@RequestParam(required = false) MembershipTier membershipTier) {
		log.info("Price request for product: {}, user: {}, tier: {}", productId, userId, membershipTier);
		PriceCalculationRequest request = PriceCalculationRequest.builder().productId(productId).userId(userId)
				.membershipTier(membershipTier).build();
		return priceCalculationService.calculatePrice(request);
	}

	@GetMapping("/{productId}/realtime")
	public Mono<PriceResponse> getRealTimePrice(@PathVariable String productId, @RequestParam(required = false) String userId) {
		log.info("Real time Price request for product: {}", productId);
		PriceCalculationRequest request = PriceCalculationRequest.builder().productId(productId).userId(userId).build();
		return priceCalculationService.calculatePriceRealTime(request);
	}
	
	@GetMapping("/price")
	public Mono<PriceResponse> getProductPriceForUserType(@RequestParam String productId, @RequestParam(required = false) MembershipTier userType) {
		log.info("Real time Price request for product: {}", productId);
		PriceCalculationRequest request = PriceCalculationRequest.builder().productId(productId).membershipTier(userType).build();
		return priceCalculationService.calculatePriceRealTime(request);
	}
	
	@GetMapping("/price/simulate")
	public Mono<PriceResponse> getProductPriceForUserTypeSimulation(@RequestParam String productId, @RequestParam MembershipTier userType,
			@RequestParam LocalDate date) {
		log.info("Real time Price request for product: {}", productId);
		PriceCalculationRequest request = PriceCalculationRequest.builder().productId(productId).membershipTier(userType).date(date).build();
		return priceCalculationService.calculatePriceRealTime(request);
	}
	
	@GetMapping(value = "/{productId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<PriceResponse> getPriceStream(@PathVariable String productId) {
		log.info("SSE stream started for product: {}", productId);
		return priceCalculationService.getPriceUpdateStream(productId);
	}
	
	@GetMapping("/{productId}/cache")
	public Mono<Void> invalidateCache(@PathVariable String productId) {
		log.info("Invalidate cache for product: {}", productId);
		return priceCalculationService.invalidateCache(productId);
	}
}
