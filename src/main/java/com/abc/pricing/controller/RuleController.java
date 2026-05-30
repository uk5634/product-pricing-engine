package com.abc.pricing.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abc.pricing.domain.PricingRuleEntity;
import com.abc.pricing.domain.RuleInfo;
import com.abc.pricing.dto.CreateRuleRequest;
import com.abc.pricing.rule.PricingRule;
import com.abc.pricing.service.RuleEngineService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {
	
	@Autowired
	private RuleEngineService ruleEngineService;
	
	@GetMapping
	public List<RuleInfo> getAllRules(){
		return ruleEngineService.getAllRules().stream()
				.map(rule -> RuleInfo.builder()
						.name(rule.getName())
						.ruleType(rule.getRuleType())
						.priority(rule.getPriority())
						.enabled(rule.isEnabled())
						.dynamic(isDynamic(rule))
						.build()
					).toList();
	}
	
	@GetMapping("/enabled")
	public List<RuleInfo> getEnabledRules(){
		return ruleEngineService.getEnabledRules().stream()
				.map(rule -> RuleInfo.builder()
						.name(rule.getName())
						.ruleType(rule.getRuleType())
						.priority(rule.getPriority())
						.enabled(rule.isEnabled())
						.dynamic(isDynamic(rule))
						.build()
					).toList();
	}
	
	@GetMapping("/dynamic")
	public Flux<PricingRuleEntity> getDynamicRules(){
		return ruleEngineService.getDynamicRules();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<PricingRuleEntity> createRule(@Valid @RequestBody CreateRuleRequest request){
		log.info("Creating dynamic rule: {}", request.getName());
		
		PricingRuleEntity entity = PricingRuleEntity.builder()
				.id(UUID.randomUUID().toString())
				.name(request.getName())
				.description(request.getDescription())
				.ruleType(request.getRuleType())
				.priority(request.getPriority())
				.enabled(true)
				.configuration(request.getConfiguration())
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();
		
		return ruleEngineService.createRule(entity);
	}

	
	private boolean isDynamic(PricingRule rule) {
		return rule.getClass().getSimpleName().contains("Dynamic");
	}
	
}
