package com.abc.pricing.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import com.abc.pricing.dto.PriceResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/*
 * Multi-layer caching service for price data
 * L1: local caffeine cache (fast, limited size)
 * L2: Redis distributed cache (shared across instances)
 */
@Slf4j
@Service
public class PriceCacheService {
	 
	private final ReactiveRedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;
	
	private Cache<String, PriceResponse> localCache;
	
	@Value("${pricing.cache.local.enabled:true}")
	private boolean localCacheEnabled;
	
	@Value("${pricing.cache.local.ttl-seconds:60}")
	private int localCacheTtlSeconds;
	
	@Value("${pricing.cache.local.max-size:10000}")
	private int localCacheMaxSize;
	
	@Value("${pricing.cache.distributed.enabled:true}")
	private boolean distributedCacheEnabled;
	
	@Value("${pricing.cache.distributed.ttl-seconds:300}")
	private int distributedCacheTtlSeconds;
	
	@Value("${pricing.cache.distributed.key-prefix:pricing:}")
	private String keyPrefix;

	public PriceCacheService(ReactiveRedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
		this.objectMapper.registerModule(new JavaTimeModule());
	}
	
	@PostConstruct
	public void init() {
		this.localCache = Caffeine.newBuilder()
				.maximumSize(localCacheMaxSize)
				.expireAfterWrite(localCacheTtlSeconds, TimeUnit.SECONDS)
				.recordStats()
				.build();
		log.info("Price cache initialized - Local: {} (TTL: {}s, max: {}), Distributed: {} (TTL: {}s)",
				localCacheEnabled, localCacheTtlSeconds, localCacheMaxSize, distributedCacheEnabled, distributedCacheTtlSeconds);
	}
	
	/*
	 * Get Price from cache (L1 then L2)
	 */
	public Mono<PriceResponse> get(String key){
		if(localCacheEnabled) {
			PriceResponse cached = localCache.getIfPresent(key);
			if(cached != null) {
				log.info("L1 cache hit for key: {}", key);
				return Mono.just(cached);
			}
		}
		
		if(distributedCacheEnabled) {
			return redisTemplate.opsForValue().get(keyPrefix+ key)
					.flatMap(json -> {
						try {
							PriceResponse response = objectMapper.readValue(json, PriceResponse.class);
							log.info("L1 cache hit for key: {}", key);
							if(localCacheEnabled) {
								localCache.put(key, response);
							}
							return Mono.just(response);
						} catch(JsonProcessingException e) {
							log.warn("Failed to deserialize cached price for key: {} - {}", key, e);
							return Mono.empty();
						}
					})
					.onErrorResume( e -> {
						log.warn("Redis unavailable, falling back to local cache only: {}", e.getMessage());
							return Mono.empty();
								}
					);
			}
		return Mono.empty();
	}
	
	/*
	 * Put price in cache (L1 and L2)
	 */
	public Mono<Void> put(String key, PriceResponse price){
		if(localCacheEnabled) {
			localCache.put(key, price);
			log.info("Cached in L1: {}", key);
		}
		if(distributedCacheEnabled) {
			try {
				String json = objectMapper.writeValueAsString(price);
				return redisTemplate.opsForValue()
						.set(keyPrefix + key, json, Duration.ofSeconds(distributedCacheTtlSeconds))
						.doOnSuccess(v -> log.info("Cached in L2 : {}",key))
						.onErrorResume( e -> {
							log.warn("Redis unavailable, skipping L2 cache: {}", e.getMessage());
							return Mono.empty();
								}
					)
						.then();
			} catch(JsonProcessingException e) {
				log.warn("Failed to serialize price price for caching: {} - {}", key, e);
			return Mono.empty();
		}
		}
		return Mono.empty();
	}
	
	/*
	 * Invalidate a specific cache key
	 */
	public Mono<Void> invalidate(String key){
		if(localCacheEnabled) {
			localCache.invalidate(key);
		}
		if(distributedCacheEnabled) {
			return redisTemplate.delete(keyPrefix+key)
					.doOnSuccess(v -> log.debug("Invalidated cache key: {}",key))
					.onErrorResume( e -> {
						log.warn("Redis unavialble, skipping L2 cache invalidation: {}", e.getMessage());
						return Mono.empty();
							}
				)
					.then();
		}
		return Mono.empty();
	}
	
	/*
	 * Invalidate all cache keys matching a pattern
	 */
	public Mono<Void> invalidateByPattern(String pattern){
		// invalidate all L1 cache , no pattern support in caffeine
		if(localCacheEnabled) {
			localCache.invalidateAll();;
		}
		if(distributedCacheEnabled) {
			return redisTemplate.keys(keyPrefix+pattern)
					.flatMap(redisTemplate::delete)
					.then()
					.doOnSuccess(v -> log.info("Invalidated cache by pattern: {}",pattern))
			.onErrorResume( e -> {
				log.warn("Redis unavailable, skipping L2 pattern invalidation: {}", e.getMessage());
				return Mono.empty();
					}
		);
		}
		return Mono.empty();
	}
	
	/*
	 * Clear all caches.
	 */
	public Mono<Void> clearAll(){
		if(localCacheEnabled) {
			localCache.invalidateAll();;
		}
		if(distributedCacheEnabled) {
			return redisTemplate.keys(keyPrefix+"*")
					.flatMap(redisTemplate::delete)
					.then()
					.doOnSuccess(v -> log.info("Cleared All price caches "))
					.onErrorResume( e -> {
						log.warn("Redis unavialable, skipping L2 cache clear: {}", e.getMessage());
						return Mono.empty();
							}
				);
		}
		return Mono.empty();
	}
	
	/*
	 * Get cache statistics.
	 */
	public CacheStats getStats() {
		var caffeineStats = localCache.stats();
		return new CacheStats(localCache.estimatedSize(), caffeineStats.hitCount(), caffeineStats.missCount(), caffeineStats.hitRate());
	}
	
	public record CacheStats(long size, long hitCount, long missCount, double hitRate) {}

}
