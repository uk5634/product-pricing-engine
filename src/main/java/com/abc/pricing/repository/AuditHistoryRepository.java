package com.abc.pricing.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.abc.pricing.domain.AuditHistory;

@Repository
public interface AuditHistoryRepository extends ReactiveCrudRepository<AuditHistory, String>{


}
