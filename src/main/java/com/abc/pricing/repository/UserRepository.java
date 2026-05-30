package com.abc.pricing.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.abc.pricing.domain.User;

import reactor.core.publisher.Mono;

/*
 * Reactive Repository for User entities
 */
@Repository
public interface UserRepository extends ReactiveCrudRepository<User, String>{

	Mono<User> findByUsername(String username);
}
