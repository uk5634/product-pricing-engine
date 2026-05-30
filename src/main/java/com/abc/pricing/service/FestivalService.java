package com.abc.pricing.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abc.pricing.domain.Festival;
import com.abc.pricing.dto.CreateFestivalRequest;
import com.abc.pricing.repository.FestivalRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalService {

	@Autowired
	private FestivalRepository festivalRepository;
	
	@Autowired
	private PriceCacheService priceCacheService;
	
	/*
	 * Get all festivals
	 */
	public Flux<Festival> getAllFestivals() {
		return festivalRepository.findAll();
	}

	/*
	 * Get Festival by Id
	 */
	public Mono<Festival> getFestival(String festivalId) {
		return festivalRepository.findById(festivalId)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Festival not found: "+ festivalId)));
	}

	/*
	 * Get Active Festivals for today
	 */
	public Flux<Festival> getActiveFestivals() {
		return festivalRepository.findActiveFestivals(LocalDate.now());
	}
	
	/*
	 * Get Active Festivals for today
	 */
	public Flux<Festival> getActiveFestivalsForDate(LocalDate date) {
		return festivalRepository.findActiveFestivals(date);
	}

	/*
	 * Create a new festival
	 */
	public Mono<Festival> createFestival(@Valid CreateFestivalRequest request) {
		if(request.getEndDate().isBefore(request.getStartDate())) {
			return Mono.error(new IllegalArgumentException("End date must be after start date"));
		}
		Festival festival = Festival.builder()
				.id(UUID.randomUUID().toString())
				.name(request.getName())
				.startDate(request.getStartDate())
				.endDate(request.getEndDate())
				.discountPercentage(request.getDiscountPercentage())
				.enabled(true)
				.createdAt(LocalDateTime.now())
				.build();
		return festivalRepository.save(festival)
				.doOnSuccess(f -> {
					log.info("Created festival: {} ({} - {})", f.getName(), f.getStartDate(), f.getEndDate());
					if(f.isActiveOn(LocalDate.now())) {
						priceCacheService.clearAll().subscribe();
					}
				});
	}

}
