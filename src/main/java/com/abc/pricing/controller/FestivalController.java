package com.abc.pricing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abc.pricing.domain.Festival;
import com.abc.pricing.dto.CreateFestivalRequest;
import com.abc.pricing.service.FestivalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
public class FestivalController {

	@Autowired
	private FestivalService festivalService;
	
	@GetMapping
	public Flux<Festival> getAllFestivals(){
		return festivalService.getAllFestivals();
	}
	
	@GetMapping("/active")
	public Flux<Festival> getActiveFestivals(){
		return festivalService.getActiveFestivals();
	}
	
	@GetMapping("/{festivalId}")
	public Mono<Festival> getFestival(@PathVariable String festivalId){
		return festivalService.getFestival(festivalId);
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Festival> createFestival(@Valid @RequestBody CreateFestivalRequest request){
		log.info("Creating festival: {} ({} - {}", request.getName(), request.getStartDate(), request.getEndDate());
		return festivalService.createFestival(request);
	}
	
	
}
