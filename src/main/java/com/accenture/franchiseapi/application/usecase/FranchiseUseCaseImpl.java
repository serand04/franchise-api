package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.domain.model.TopStockProduct;
import com.accenture.franchiseapi.domain.port.in.FranchiseUseCase;
import com.accenture.franchiseapi.domain.port.out.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FranchiseUseCaseImpl implements FranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    @Override
    public Mono<Franchise> addFranchise(String name) {
        Franchise franchise = Franchise.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .build();
        return franchiseRepository.save(franchise);
    }

    @Override
    public Mono<Franchise> updateFranchiseName(String franchiseId, String newName) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found: " + franchiseId)))
                .map(franchise -> {
                    franchise.setName(newName);
                    return franchise;
                })
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Mono<Franchise> addBranch(String franchiseId, String branchName) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found: " + franchiseId)))
                .map(franchise -> {
                    Branch branch = Branch.builder()
                            .id(UUID.randomUUID().toString())
                            .name(branchName)
                            .build();
                    franchise.getBranches().add(branch);
                    return franchise;
                })
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Mono<Franchise> updateBranchName(String franchiseId, String branchId, String newName) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found: " + franchiseId)))
                .map(franchise -> {
                    franchise.getBranches().stream()
                            .filter(b -> b.getId().equals(branchId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId))
                            .setName(newName);
                    return franchise
                })
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Mono<Franchise> addProduct(String franchiseId, String branchId, String productName, int stock) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found: " + franchiseId)))
                .map(franchise -> {
                    Branch branch = franchise.getBranches().stream()
                            .filter(b -> b.getId().equals(branchId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Branch not foud: " + branchId));
                    Product product = Product.builder()
                            .id(UUID.randomUUID().toString())
                            .name(productName)
                            .stock(stock)
                            .build();
                    branch.getProducts().add(product);
                    return franchise;
                })
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Mono<Franchise> removeProduct(String franchiseId, String branchId, String productId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found: " + franchiseId)))
                .map(franchise ->  {
                    Branch branch = franchise.getBranches().stream()
                            .filter(b -> b.getId().equals(branchId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
                    branch.getProducts().removeIf(p -> p.getId().equals(productId)):
                    return franchise;
                })
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Mono<Franchise> updateProductStock(String franchiseId, String branchId, String productId, int newStock) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found: " + franchiseId)))
                .map(franchise ->  {
                    franchise.getBranches().stream()
                            .filter(b -> b.getId().equals(branchId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Branch not douf: " + branchId))
                            .getProducts().stream()
                            .filter(p -> p.getId().equals(productId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Product not found: " + productId))
                            .setStock(newStock);
                    return franchise;
                })
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Mono<Franchise> updateProductName(String franchiseId, String branchId, String productId, String newName) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found: " + franchiseId)))
                .map(franchise -> {
                    franchise.getBranches().stream()
                            .filter(b -> b.getId().equals(branchId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId))
                            .getProducts().stream()
                            .filter(p -> p.getId().equals(productId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Product not found: " + productId))
                            .setName(newName);
                    return franchise
                })
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Flux<TopStockProduct> getTopStockProductsPerBranch(String franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found: " + franchiseId)))
                .flatMapMany(franchise -> Flux.fromIterable(franchise.getBranches()))
                .flatMap(branch -> Flux.fromIterable(branch.getProducts())
                        .filter(p -> p.getStock() > 0)
                        .sort(Comparator.comparingInt(Product::getStock).reversed())
                        .next()
                        .map(product -> TopStockProduct.builder()
                                .branchId(branch.getId())
                                .branchName(branch.getName())
                                .productId(product.getId())
                                .productName(product.getName())
                                .stock(product.getStock())
                                .build()));
    }

}
