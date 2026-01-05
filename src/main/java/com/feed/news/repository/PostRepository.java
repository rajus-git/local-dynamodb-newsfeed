package com.feed.news.repository;

import com.feed.news.domain.Post;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;

@Repository
public class PostRepository {

    private static final String TABLE_NAME = "posts";
    private static final String CREATOR_INDEX = "creatorId-createdAt-index";

    private final DynamoDbTable<Post> postTable;
    private final DynamoDbIndex<Post> creatorIndex;

    public PostRepository(DynamoDbEnhancedClient enhancedClient) {
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
}
