package com.abc.pricing.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abc.pricing.domain.Product;
import com.abc.pricing.dto.CreateProductRequest;
import com.abc.pricing.repository.ProductRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
	
	@Autowired
	private ProductRepository productRepository;
	
	/*
	 * Get All products
	 */
	public Flux<Product> getAllProducts() {
		return productRepository.findAll();
	}

	/*
	 * Get product by ID
	 */
	public Mono<Product> getProduct(String productId) {
		return productRepository.findById(productId)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Product not found: "+ productId)));
	}

	/*
	 * Create a new product
	 */
	public Mono<Product> createProduct(@Valid CreateProductRequest request) {
		return productRepository.save(
				Product.builder()
				.id(UUID.randomUUID().toString())
				.name(request.getName())
				.description(request.getDescription())
				.basePrice(request.getBasePrice())
				.category(request.getCategory())
				.inventoryCount(request.getInventoryCount())
				.totalInventory(request.getTotalInventory())
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build()
				)
				.doOnSuccess(p -> log.info("Created product: {} ({})", p.getName(), p.getId()));
	}

	/*
	 * Update product base price
	 */
	public Mono<Long> updateBasePrice(String productId, BigDecimal newPrice) {
		return productRepository.updateBasePrice(productId, newPrice);
	}

}
