package com.abc.pricing.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.abc.pricing.domain.PricingRuleEntity;
import com.abc.pricing.dto.PricingContext;
import com.abc.pricing.repository.PricingRuleRepository;
import com.abc.pricing.rule.AbstractPricingRule;
import com.abc.pricing.rule.CompetitivePricingRule;
import com.abc.pricing.rule.DynamicPricingRule;
import com.abc.pricing.rule.FestivalPricingRule;
import com.abc.pricing.rule.InventoryPricingRule;
import com.abc.pricing.rule.PricingRule;
import com.abc.pricing.rule.UserLoyaltyPricingRule;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * Rule Engine that manages and applies pricing rules in priority order.
 */
@Slf4j
@Service
public class RuleEngineService {

	private final List<PricingRule> staticRules;
	private final List<DynamicPricingRule> dynamicRules;
	private final PricingRuleRepository pricingRuleRepository;
	
	public RuleEngineService(
				CompetitivePricingRule competitivePricingRule,
				InventoryPricingRule inventoryPricingRule,
				UserLoyaltyPricingRule userLoyaltyPricingRule,
				FestivalPricingRule festivalPricingRule,
				PricingRuleRepository pricingRuleRepository
			) {
		this.staticRules = new ArrayList<>();
		this.staticRules.add(competitivePricingRule);
		this.staticRules.add(inventoryPricingRule);
		this.staticRules.add(userLoyaltyPricingRule);
		this.staticRules.add(festivalPricingRule);
		
		this.dynamicRules = new CopyOnWriteArrayList<>();
		this.pricingRuleRepository = pricingRuleRepository;
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		loadDynamicRules().subscribe(
				count -> log.info("Loaded {} dynamic pricing rules",count),
				error -> log.warn("Could not load dynamic rules: {}", error.getMessage())
				);
	}
	
	/*
	 * Load dynamic rules from the database.
	 */
	public Mono<Long> loadDynamicRules(){
		return pricingRuleRepository.findAllEnabledOrderByPriority()
				.onErrorResume(e -> {
					log.warn("Error loading dynamic rules, table may not exist yet: {}", e.getMessage());
					return Flux.empty();
				})
				.map(this::convertToRule)
				.doOnNext(rule -> {
					dynamicRules.removeIf(r -> r.getName().equals(rule.getName()));
					dynamicRules.add(rule);
				})
				.count()
				.doOnSuccess(count -> log.debug("Loaded {} dynamic rules",count));
	}
	
	private DynamicPricingRule convertToRule(PricingRuleEntity entity) {
		return new DynamicPricingRule(
				entity.getId(), 
				entity.getName(), 
				entity.getRuleType(),
				entity.getPriority(), 
				entity.getEnabled(), 
				entity.getConfiguration());
	}
	
	/*
	 * Get all rules sorted by priority.
	 */
	public List<PricingRule> getAllRules(){
		List<PricingRule> allRules = new ArrayList<>(staticRules);
		allRules.addAll(dynamicRules);
		allRules.sort(Comparator.comparingInt(PricingRule::getPriority));
		return allRules;
	}
	
	/*
	 * Get only enabled rules sorted by priority.
	 */
	public List<PricingRule> getEnabledRules(){
		return getAllRules().stream()
				.filter(PricingRule::isEnabled)
				.toList();
	}
	
	/*
	 * Toggle a rules's enabled status
	 */
	public Mono<Void> toggleRule(String ruleId, boolean enabled){
		for(PricingRule rule: staticRules) {
			if(rule.getName().equals(ruleId)) {
				if(rule instanceof AbstractPricingRule abstractRule) {
					abstractRule.setEnabled(enabled);
					log.info("Toggled static rule '{}' to {}", rule.getName(), enabled);
					return Mono.empty();
				}
			}
		}
		return pricingRuleRepository.updateEnabled(ruleId, enabled)
				.doOnSuccess(v -> {
					dynamicRules.stream()
					.filter(r -> r.getName().equals(ruleId))
					.findFirst()
					.ifPresent(r -> r.setEnabled(enabled));
					log.info("Toggled dynamic rule '{}', to {}", ruleId, enabled);
				});
	}
	
	/*
	 * Refresh dynamic rules from database.
	 */
	public Mono<Long> refreshDynamicRules(){
		dynamicRules.clear();
		return loadDynamicRules();
	}
	
	/*
	 * Remove a dynamic rule.
	 */
	public Mono<Void> removeDynamicRule(String ruleId){
		return pricingRuleRepository.deleteById(ruleId)
				.doOnSuccess(v -> {
					dynamicRules.removeIf(r -> r.getName().equals(ruleId));
					log.info("Removed dynamic rule: {}", ruleId);
				});
	}
	
	/*
	 * Add a new dynamic rule.
	 */
	public Mono<DynamicPricingRule> addDynamicRule(PricingRuleEntity entity){
		return pricingRuleRepository.save(entity)
				.map(saved -> {
					DynamicPricingRule rule = convertToRule(saved);
					dynamicRules.add(rule);
					log.info("Added dynamic rule: {} with priority {}", rule.getName(), rule.getPriority());
					return rule;
				});
	}
	
	/*
	 * Apply all enabled rules to the pricing context.
	 */
	public Mono<PricingContext> applyRules(PricingContext context){
		List<PricingRule> rules = getEnabledRules();
		log.info("Applying {} rules to product {}", rules.size(), context.getProductId());
		if(context.getAppliedRules() == null) {
			context.setAppliedRules(new ArrayList<>());
		}
		return Flux.fromIterable(rules)
				.reduce(Mono.just(context), (ctxMono, rule) -> ctxMono.flatMap(rule::apply))
				.flatMap(mono -> mono)
				.doOnSuccess(ctx -> log.info("Rules applied. Final price: {}",ctx.getCurrentPrice() +" base: "+ ctx.getBasePrice()));
	}
	
	/*
	 * Create Rule , save into DB
	 */
	public Mono<PricingRuleEntity> createRule(PricingRuleEntity entity){
		return pricingRuleRepository.save(entity)
				.doOnSuccess(saved -> loadDynamicRules().subscribe());
	}
	
	/*
	 * Fetch dynamic rules from db
	 */
	public Flux<PricingRuleEntity> getDynamicRules(){
		return pricingRuleRepository.findAll();
	}
}
