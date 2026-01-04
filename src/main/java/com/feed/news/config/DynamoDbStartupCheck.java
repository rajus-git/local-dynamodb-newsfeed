package com.feed.news.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Component
@Profile("local")
public class DynamoDbStartupCheck implements ApplicationRunner {

    private final DynamoDbClient dynamoDbClient;

    public DynamoDbStartupCheck(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            dynamoDbClient.listTables();
            System.out.println("✅ Successfully connected to DynamoDB Local");
        } catch (Exception e) {
            System.err.println("❌ Failed to connect to DynamoDB Local");
            throw e; // fail fast
        }
    }
}
