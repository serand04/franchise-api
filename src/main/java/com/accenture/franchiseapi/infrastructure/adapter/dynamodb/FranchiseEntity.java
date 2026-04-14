package com.accenture.franchiseapi.infrastructure.adapter.dynamodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.ArrayList;
import java.util.List;

@DynamoDbBean
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FranchiseEntity {
    private String id;
    private String name;
    private List<BranchEntity> branches;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() { return id; }

    @DynamoDbAttribute("name")
    public String getName() { return name; }

    @DynamoDbAttribute("branches")
    public List<BranchEntity> getBranches() {
        return branches != null ? branches : new ArrayList<>();
    }

    // ── Nested beans ─────────────────────────────────────────────────────────

    @DynamoDbBean
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchEntity {
        private String id;
        private String name;
        private List<ProductEntity> products;

        @DynamoDbAttribute("id")
        public String getId() { return id; }

        @DynamoDbAttribute("name")
        public String getName() { return name; }

        @DynamoDbAttribute("products")
        public List<ProductEntity> getProducts() {
            return products != null ? products : new ArrayList<>();
        }
    }

    @DynamoDbBean
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductEntity {
        private String id;
        private String name;
        private int stock;

        @DynamoDbAttribute("id")
        public String getId() { return id; }

        @DynamoDbAttribute("name")
        public String getName() { return name; }

        @DynamoDbAttribute("stock")
        public int getStock() { return stock; }
    }
}
