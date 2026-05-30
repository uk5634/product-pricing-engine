package com.abc.pricing.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.abc.pricing.domain.AuditHistory;
import com.abc.pricing.domain.MembershipTier;
import com.abc.pricing.domain.Product;
import com.abc.pricing.dto.PriceCalculationRequest;
import com.abc.pricing.dto.PriceResponse;
import com.abc.pricing.dto.PricingContext;
import com.abc.pricing.repository.AuditHistoryRepository;
import com.abc.pricing.repository.CompetitorPriceRepository;
import com.abc.pricing.repository.FestivalRepository;
import com.abc.pricing.repository.ProductRepository;
import com.abc.pricing.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/*
 * Main service for calculating product pricces using the rule engine.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCalculationService {

	private final ProductRepository productRepository;
	private final CompetitorPriceRepository competitorPriceRepository;
	private final FestivalRepository festivalRepository;
	private final UserRepository userRepository;
	private final RuleEngineService ruleEngineService;
	private final PriceCacheService priceCacheService;
	private final AuditHistoryRepository auditHistoryRepository;
	
	// Sink for broadcasting price updates
	private final Sinks.Many<PriceResponse> priceUpdateSink = Sinks.many().multicast().onBackpressureBuffer();
	
	/*
	 * Calculate the price for a product.
	 */
	public Mono<PriceResponse> calculatePrice(String productId, String userId) {
		return calculatePrice(PriceCalculationRequest.builder()
				.productId(productId)
				.userId(userId)
				.build());
	}
	
	/*
	 * Calculate the price for a product with full context.
	 */
	public Mono<PriceResponse> calculatePrice(PriceCalculationRequest request) {
		String cacheKey = buildCacheKey(request);
		
		return priceCacheService.get(cacheKey)
				.map(cached -> {
					cached.setCacheStatus("HIT");
					return cached;
				})
				.switchIfEmpty(
						calculatePriceInternal(request)
							.flatMap(response -> {
								response.setCacheStatus("MISS");
								return priceCacheService.put(cacheKey, response)
										.thenReturn(response);
							})
						)
				;
	}

	private Mono<PriceResponse> calculatePriceInternal(PriceCalculationRequest request) {
		return buildPricingContext(request)
				.flatMap(ruleEngineService::applyRules)
				.map(this::auditCall)
				.map(this::buildPriceResponse);
	}
	
	/*
	 * Calculate price without caching (for real-time update)
	 */
	public Mono<PriceResponse> calculatePriceRealTime(PriceCalculationRequest request) {
		return calculatePriceInternal(request)
				.doOnNext(response ->{
					priceUpdateSink.tryEmitNext(response);
					priceCacheService.invalidate(buildCacheKey(request)).subscribe();
					});
	}
	
	private PricingContext auditCall(PricingContext context) {
		BigDecimal totalDiscount = context.getBasePrice().subtract(context.getCurrentPrice());
		if(totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
			// price decreased
		} else {
			totalDiscount = BigDecimal.ZERO;		
		}
		BigDecimal totalAdjustment = context.getCurrentPrice().subtract(context.getBasePrice());
		
		 auditHistoryRepository.save(AuditHistory.builder()
				 .id(UUID.randomUUID().toString())
				 .productId(context.getProductId())
				 .productName(context.getProductName())
				 .basePrice(context.getBasePrice())
				 .finalPrice(context.getCurrentPrice())
				 .totalDiscount(totalDiscount)
				 .totalAdjustment(totalAdjustment)
				 .userId(context.getUserId())
				 .membershipTier(context.getMembershipTier().toString())		 
				 .createdAt(LocalDateTime.now())
				 .build())
		 		 .subscribe();
		return context;
				
	}

	
	private PriceResponse buildPriceResponse(PricingContext context) {
		BigDecimal totalDiscount = context.getBasePrice().subtract(context.getCurrentPrice());
		if(totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
			// price decreased
		} else {
			totalDiscount = BigDecimal.ZERO;		
		}
		BigDecimal totalAdjustment = context.getCurrentPrice().subtract(context.getBasePrice());
		return PriceResponse.builder()
				.productId(context.getProductId())
				.productName(context.getProductName())
				.basePrice(context.getBasePrice())
				.finalPrice(context.getCurrentPrice())
				.totalDiscount(totalDiscount)
				.totalAdjustment(totalAdjustment)
				.appliedRules(context.getAppliedRules())
				.calculatedAt(LocalDateTime.now())	
				.build();
				
	}

	/*
	 * Build the pricing context with all necessary data.
	 */
	private Mono<PricingContext> buildPricingContext(PriceCalculationRequest request) {
		String productId = request.getProductId();
		
		// Fetch product
		Mono<Product> productMono = productRepository.findById(productId)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Product not found: "+productId)));
				
		// Fetch competitor prices
		Mono<List<PricingContext.CompetitorPriceInfo>> competitorPricesMono =
				competitorPriceRepository.findByProductId(productId)
				.map(cp -> PricingContext.CompetitorPriceInfo.builder().competitorName(cp.getCompetitorName()).price(cp.getPrice()).build())
				.collectList();
		
		// Fetch average and lowest competitor prices
		Mono<BigDecimal> avgPriceMono = competitorPriceRepository.findAveragePrice(productId)
				.defaultIfEmpty(BigDecimal.ZERO);
		
		Mono<BigDecimal> lowestPriceMono = competitorPriceRepository.findLowestPrice(productId)
				.defaultIfEmpty(BigDecimal.ZERO);
		
		// Fetch active festivals
		
		LocalDate today = request.getDate() != null ? request.getDate() : LocalDate.now();
		Mono<List<PricingContext.FestivalInfo>> festivalsMono =
				festivalRepository.findActiveFestivals(today)
				.map(f -> PricingContext.FestivalInfo.builder().name(f.getName()).discountPercentage(f.getDiscountPercentage()).build())
				.collectList();
		
		// Determine membership tier
		Mono<MembershipTier> membershipTierMono;
		if(request.getMembershipTier() != null) {
			membershipTierMono = Mono.just(request.getMembershipTier());
		} else if(request.getUserId() != null) {
			membershipTierMono = Mono.just(userRepository.findById(request.getUserId()).block().getMembershipTier());
		} else {
			membershipTierMono = Mono.just(MembershipTier.STANDARD);
		}
		
		// Combine all data
		return Mono.zip(productMono, competitorPricesMono, avgPriceMono, lowestPriceMono, festivalsMono, membershipTierMono)
				.map(tuple -> {
					Product product = tuple.getT1();
					return PricingContext.builder()
							.productId(product.getId())
							.productName(product.getName())
							.category(product.getCategory())
							.basePrice(product.getBasePrice())
							.currentPrice(product.getBasePrice())
							.inventoryCount(product.getInventoryCount())
							.totalInventory(product.getTotalInventory())
							.userId(request.getUserId())
							.membershipTier(tuple.getT6())
							.competitorPrices(tuple.getT2())
							.averageCompetitorPrice(tuple.getT3())
							.lowestCompetitorPrice(tuple.getT4())
							.calculationDate(today)
							.activeFestivals(tuple.getT5())
							.appliedRules(new ArrayList<>())
							.build();
				});
	}

	public Mono<Void> invalidateCache(String productId){
		return priceCacheService.invalidateByPattern("price:"+productId+"*");
	}
	
	/*
	 * Get a stream of price updates for a product.
	 */
	public Flux<PriceResponse> getPriceUpdateStream(String productId){
		return priceUpdateSink.asFlux()
				.filter(response -> response.getProductId().equals(productId));
	}
	
	/*
	 * Invalidate price cache for a product.
	 */
	public String buildCacheKey(PriceCalculationRequest request) {
		StringBuilder key = new StringBuilder("price:");
		key.append(request.getProductId());
		if(request.getUserId() != null) {
			key.append(":user:").append(request.getUserId());
		}
		if(request.getMembershipTier() != null) {
			key.append(":tier:").append(request.getMembershipTier().name());
		}
		return key.toString();
	}

}
