package com.abc.pricing.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abc.pricing.domain.CompetitorPrice;
import com.abc.pricing.service.CompetitorPriceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/competitors")
@RequiredArgsConstructor
public class CompetitorPriceController {
	
	@Autowired
	private CompetitorPriceService competitorPriceService;
	
	
	@GetMapping("/products/{productId}")
	public Flux<CompetitorPrice> getCompetitorPrices(@PathVariable String productId){
		return competitorPriceService.getCompetitorPrices(productId);
	}
	
	@GetMapping("/product/price")
	public Flux<CompetitorPrice> getCompPrice(@RequestParam String productId){
		return competitorPriceService.getCompetitorPrices(productId);
	}

	@PostMapping("/products/{productId}")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<CompetitorPrice> createCompetitorPrice(
			@PathVariable String productId,
			@RequestParam String competitorName,
			@RequestParam BigDecimal price){
		log.info("Updating competitor price: {} for product {} = {}", competitorName, productId, price );
		return competitorPriceService.updateCompetitorPrice(productId, competitorName, price);	
	}
	
	@PutMapping("/products/{productId}")
	@ResponseStatus(HttpStatus.OK)
	public Mono<CompetitorPrice> updateCompetitorPrice(
			@PathVariable String productId,
			@RequestParam String competitorName,
			@RequestParam BigDecimal price){
		log.info("Updating competitor price: {} for product {} = {}", competitorName, productId, price );
		return competitorPriceService.updateCompetitorPrice(productId, competitorName, price);	
	}
	
	
			
}
