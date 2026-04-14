package com.accenture.franchiseapi.infrastructure.adapter.dynamodb;

import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.port.out.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@RequiredArgsConstructor
public class DynamoDbFranchiseRepository implements FranchiseRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final FranchiseMapper mapper;

    private DynamoDbTable<FranchiseEntity> table() {
        return enhancedClient.table("franchises", TableSchema.fromBean(FranchiseEntity.class));
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return Mono.fromCallable(() -> {
                    FranchiseEntity entity = mapper.toEntity(franchise);
                    table().putItem(entity);
                    return mapper.toDomain(entity);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return Mono.fromCallable(() -> {
                    Key key = Key.builder().partitionValue(id).build();
                    FranchiseEntity entity = table().getItem(key);
                    if (entity == null) return null;
                    return mapper.toDomain(entity);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return Mono.fromRunnable(() -> {
                    Key key = Key.builder().partitionValue(id).build();
                    table().deleteItem(key);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
