package com.feed.news.repository;

import com.feed.news.domain.Post;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class PostRepository {

    private static final String TABLE_NAME = "posts";
    private static final String CREATOR_INDEX = "creatorId-createdAt-index";

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Post> postTable;
    private final DynamoDbIndex<Post> creatorIndex;

    public PostRepository(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
        this.postTable =
                enhancedClient.table(TABLE_NAME, TableSchema.fromBean(Post.class));
        this.creatorIndex = postTable.index(CREATOR_INDEX);
    }

    public void save(Post post) {
        postTable.putItem(post);
    }

    public Post getById(String id) {
        return postTable.getItem(
                Key.builder()
                        .partitionValue(id)
                        .build()
        );
    }

    public Map<String, Post> batchGetByIds(List<String> postIds) {

        ReadBatch.Builder<Post> readBatch =
                ReadBatch.builder(Post.class)
                        .mappedTableResource(postTable);

        postIds.forEach(id ->
                readBatch.addGetItem(
                        Key.builder().partitionValue(id).build()
                )
        );

        BatchGetItemEnhancedRequest request =
                BatchGetItemEnhancedRequest.builder()
                        .readBatches(readBatch.build())
                        .build();

        return enhancedClient.batchGetItem(request)
                .resultsForTable(postTable)
                .stream()
                .collect(Collectors.toMap(Post::getId, p -> p));
    }

    public List<Post> getRecentPostsByCreator(String creatorId, int limit) {

        QueryConditional condition =
                QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(creatorId)
                                .build()
                );

        Page<Post> page =
                creatorIndex.query(r -> r
                        .queryConditional(condition)
                        .scanIndexForward(false) // newest first
                        .limit(limit)
                ).iterator().next();

        return page.items();
    }

    public PagedResult<Post> getRecentPostsByCreator(
            String creatorId,
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
                        Key.builder().partitionValue(creatorId).build()
                );

        Page<Post> page =
                creatorIndex.query(r -> r
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
