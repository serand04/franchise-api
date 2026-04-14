package com.accenture.franchiseapi.domain.port.out;

import com.accenture.franchiseapi.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {
    Mono<Franchise> save(Franchise franchise);
    Mono<Franchise> findById(String id);
    Mono<Void> deleteById(String id);
}
