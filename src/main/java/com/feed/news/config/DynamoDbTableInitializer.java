package com.feed.news.config;

import com.feed.news.domain.PrecomputedFeed;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.feed.news.domain.Follow;
import com.feed.news.domain.Post;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

@Component
@Profile("local")
public class DynamoDbTableInitializer implements ApplicationRunner {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbClient dynamoDbClient;

    public DynamoDbTableInitializer(
            DynamoDbEnhancedClient enhancedClient,
            DynamoDbClient dynamoDbClient) {

        this.enhancedClient = enhancedClient;
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        createPostsTable();
        createFollowsTable();
        createPrecomputedFeedTable();
    }

    private void createPostsTable() {

        DynamoDbTable<Post> table =
                enhancedClient.table("posts", TableSchema.fromBean(Post.class));

        try {
            table.createTable(builder -> builder
                    .provisionedThroughput(
                            p -> p.readCapacityUnits(5L).writeCapacityUnits(5L)
                    )
                    .globalSecondaryIndices(
                            gsi -> gsi
                                    .indexName("creatorId-createdAt-index")
                                    .projection(
                                            p -> p.projectionType(ProjectionType.ALL)
                                    )
                                    .provisionedThroughput(
                                            p -> p.readCapacityUnits(5L).writeCapacityUnits(5L)
                                    )
                    )
            );

            waitForTable("posts");
            System.out.println("✅ Created table: posts");

        } catch (ResourceInUseException e) {
            System.out.println("ℹ️ Table already exists: posts");
        }
    }

    private void createFollowsTable() {

        DynamoDbTable<Follow> table =
                enhancedClient.table("follows", TableSchema.fromBean(Follow.class));

        try {
            table.createTable(builder -> builder
                    .provisionedThroughput(
                            p -> p.readCapacityUnits(5L).writeCapacityUnits(5L)
                    )
                    .globalSecondaryIndices(
                            gsi -> gsi
                                    .indexName("userFollowed-userFollowing-index")
                                    .projection(
                                            p -> p.projectionType(ProjectionType.ALL)
                                    )
                                    .provisionedThroughput(
                                            p -> p.readCapacityUnits(5L).writeCapacityUnits(5L)
                                    )
                    )
            );

            waitForTable("follows");
            System.out.println("✅ Created table: follows");

        } catch (ResourceInUseException e) {
            System.out.println("ℹ️ Table already exists: follows");
        }
    }

    private void createPrecomputedFeedTable() {

        DynamoDbTable<PrecomputedFeed> table =
                enhancedClient.table(
                        "precomputedFeed",
                        TableSchema.fromBean(PrecomputedFeed.class)
                );

        try {
            table.createTable(b -> b
                    .provisionedThroughput(t ->
                            t.readCapacityUnits(5L)
                                    .writeCapacityUnits(5L)
                    )
            );

            waitForTable("precomputedFeed");
            System.out.println("✅ Created table: precomputedFeed");

        } catch (ResourceInUseException e) {
            System.out.println("ℹ️ Table already exists: precomputedFeed");
        }
    }


    private void waitForTable(String tableName) {
        dynamoDbClient.waiter().waitUntilTableExists(
                DescribeTableRequest.builder()
                        .tableName(tableName)
                        .build()
        );
    }
}
