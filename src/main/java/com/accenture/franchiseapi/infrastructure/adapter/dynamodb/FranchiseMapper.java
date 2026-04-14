package com.accenture.franchiseapi.infrastructure.adapter.dynamodb;

import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.model.Product;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class FranchiseMapper {

    public FranchiseEntity toEntity(Franchise franchise) {
        return FranchiseEntity.builder()
                .id(franchise.getId())
                .name(franchise.getName())
                .branches(franchise.getBranches().stream()
                        .map(this::toBranchEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    public Franchise toDomain(FranchiseEntity entity) {
        return Franchise.builder()
                .id(entity.getId())
                .name(entity.getName())
                .branches(entity.getBranches().stream()
                        .map(this::toBranchDomain)
                        .collect(Collectors.toList()))
                .build();
    }

    private FranchiseEntity.BranchEntity toBranchEntity(Branch branch) {
        return FranchiseEntity.BranchEntity.builder()
                .id(branch.getId())
                .name(branch.getName())
                .products(branch.getProducts().stream()
                        .map(this::toProductEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    private Branch toBranchDomain(FranchiseEntity.BranchEntity entity) {
        return Branch.builder()
                .id(entity.getId())
                .name(entity.getName())
                .products(entity.getProducts().stream()
                        .map(this::toProductDomain)
                        .collect(Collectors.toList()))
                .build();
    }

    private FranchiseEntity.ProductEntity toProductEntity(Product product) {
        return FranchiseEntity.ProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .stock(product.getStock())
                .build();
    }

    private Product toProductDomain(FranchiseEntity.ProductEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .stock(entity.getStock())
                .build();
    }
}
