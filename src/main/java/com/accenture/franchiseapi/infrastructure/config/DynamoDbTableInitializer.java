package com.accenture.franchiseapi.infrastructure.config;

import com.accenture.franchiseapi.infrastructure.adapter.dynamodb.FranchiseEntity;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamoDbTableInitializer {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbClient dynamoDbClient;

    @PostConstruct
    public void createTableIfNotExists() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName("franchises")
                    .build();
            log.info("Dynamo table 'franchises' already exists.");
        } catch (ResourceNotFoundException e) {
            log.info("Creating DynamoDB table 'franchises'...");
            enhancedClient
                    .table("franchises", TableSchema.fromBean(FranchiseEntity.class))
                    .createTable(builder -> builder.provisionedThroughput(pt ->
                            pt.readCapacityUnits(5L).writeCapacityUnits(5L)));
            log.info("DynamoDB table 'franchises' created.");
        }
    }
}
