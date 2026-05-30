package com.abc.pricing.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abc.pricing.domain.Product;
import com.abc.pricing.dto.CreateProductRequest;
import com.abc.pricing.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

	@Autowired
	ProductService productService;
	
	@GetMapping
	public Flux<Product> getAllProducts(){
		return productService.getAllProducts();
	}
	
	@GetMapping("/{productId}")
	public Mono<Product> getProduct(@PathVariable String productId){
		return productService.getProduct(productId);
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Product> createProduct(@Valid @RequestBody CreateProductRequest request){
		log.info("Creating product: {}", request.getName());
		return productService.createProduct(request);
	}
	
	@PutMapping("/{productId}/price")
	public Mono<Long> updateBasePrice(@PathVariable String productId, @RequestParam BigDecimal price){
		log.info("Updating base price for product {}: {}", productId, price);
		return productService.updateBasePrice(productId, price);
	}
}
