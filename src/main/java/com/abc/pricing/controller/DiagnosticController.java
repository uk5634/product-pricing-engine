package com.abc.pricing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abc.pricing.dto.PriceResponse;
import com.abc.pricing.service.PriceCacheService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/*
 * Diagnostic controller for checking Redis L2 cache
 */
@Slf4j
@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticController {

    @Autowired
    PriceCacheService  priceCacheService;

    /**
     * Get value for a specific key.
     * Usage: GET /api/diagnostics/cache/myKey
     */
    @GetMapping("/cache/{key}")
    public Mono<PriceResponse> getValueByKey(@PathVariable String key) {
    	return priceCacheService.get(key);
    }

    /**
     * Get all keys matching a pattern (defaults to all keys '*').
     * Usage: GET /api/diagnostics/cache/keys?pattern=user:*
     */
    @GetMapping("/cache/keys")
    public Mono<PriceResponse> getAllKeys(@RequestParam(defaultValue = "*") String pattern) {
    	return priceCacheService.get(pattern);
    }
}
