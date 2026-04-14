package com.accenture.franchiseapi.domain.port.in;

import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.model.TopStockProduct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FranchiseUseCase {
    Mono<Franchise> addFranchise(String name);
    Mono<Franchise> updateFranchiseName(String franchiseId, String newName);
    Mono<Franchise> addBranch(String franchiseId, String branchName);
    Mono<Franchise> updateBranchName(String franchiseId, String branchId, String newName);
    Mono<Franchise> addProduct(String franchiseId, String branchId, String productName, int stock);
    Mono<Franchise> removeProduct(String franchiseId, String branchId, String productid);
    Mono<Franchise> updateProductStock(String franchiseId, String branchId, String productId, int newStock);
    Mono<Franchise> updateProductName(String franchiseId, String branchId, String productId, String newName);
    Flux<TopStockProduct> getTopStockProductsPerBranch(String franchiseId);
