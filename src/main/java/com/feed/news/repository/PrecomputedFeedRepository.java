package com.feed.news.repository;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.feed.news.domain.PrecomputedFeed;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class PrecomputedFeedRepository {

    private static final int BATCH_SIZE = 25;

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<PrecomputedFeed> table;

    public PrecomputedFeedRepository(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
        this.table = enhancedClient.table(
                "precomputedFeed",
                TableSchema.fromBean(PrecomputedFeed.class)
        );
    }

    public void addToFeeds(List<PrecomputedFeed> items) {

        List<PrecomputedFeed> buffer = new ArrayList<>(BATCH_SIZE);

        for (PrecomputedFeed item : items) {
            buffer.add(item);

            if (buffer.size() == BATCH_SIZE) {
                writeBatch(buffer);
                buffer.clear();
            }
        }

        if (!buffer.isEmpty()) {
            writeBatch(buffer);
        }
    }

    private void writeBatch(List<PrecomputedFeed> items) {

        WriteBatch.Builder<PrecomputedFeed> writeBatch =
                WriteBatch.builder(PrecomputedFeed.class)
                        .mappedTableResource(table);

        items.forEach(writeBatch::addPutItem);

        BatchWriteItemEnhancedRequest request =
                BatchWriteItemEnhancedRequest.builder()
                        .writeBatches(writeBatch.build())
                        .build();

        enhancedClient.batchWriteItem(request);
    }

    public PagedResult<PrecomputedFeed> getFeedPage(
            String userId,
            int limit,
            String nextToken) {

        Map<String, AttributeValue> exclusiveStartKey;
        // Decode token INSIDE repository
        if (nextToken != null) {
            String json = new String(
                    Base64.getDecoder().decode(nextToken),
                    StandardCharsets.UTF_8
            );
            exclusiveStartKey =
                    EnhancedDocument.fromJson(json).toMap();
        } else {
            exclusiveStartKey = null;
        }

        QueryConditional condition =
                QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(userId).build()
                );

        Page<PrecomputedFeed> page =
                table.query(r -> r
                        .queryConditional(condition)
                        .scanIndexForward(false)
                        .limit(limit)
                        .exclusiveStartKey(exclusiveStartKey)
                ).iterator().next();

        // Encode new token INSIDE repository
        String newNextToken = null;
        if (page.lastEvaluatedKey() != null && !page.lastEvaluatedKey().isEmpty()) {
            String json =
                    EnhancedDocument
                            .fromAttributeValueMap(page.lastEvaluatedKey())
                            .toJson();

            newNextToken = Base64.getEncoder()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        }

        return new PagedResult<>(page.items(), newNextToken);
    }

}
