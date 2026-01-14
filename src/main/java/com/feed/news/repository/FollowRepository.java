package com.feed.news.repository;

import com.feed.news.domain.Follow;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.Iterator;
import java.util.List;

@Repository
public class FollowRepository {

    private static final String TABLE_NAME = "follows";
    private static final String REVERSE_INDEX = "userFollowed-userFollowing-index";

    private final DynamoDbTable<Follow> followTable;
    private final DynamoDbIndex<Follow> reverseIndex;

    public FollowRepository(DynamoDbEnhancedClient enhancedClient) {
        this.followTable =
                enhancedClient.table(TABLE_NAME, TableSchema.fromBean(Follow.class));
        this.reverseIndex = followTable.index(REVERSE_INDEX);
    }

    public void follow(Follow follow) {
        followTable.putItem(follow);
    }

    public void unfollow(String userFollowing, String userFollowed) {
        followTable.deleteItem(
                Key.builder()
                        .partitionValue(userFollowing)
                        .sortValue(userFollowed)
                        .build()
        );
    }

    public boolean isFollowing(String userFollowing, String userFollowed) {
        Follow item =
                followTable.getItem(
                        Key.builder()
                                .partitionValue(userFollowing)
                                .sortValue(userFollowed)
                                .build()
                );
        return item != null;
    }

    public Iterator<Page<Follow>> iterateFollowersOf(String userId) {

        QueryConditional condition =
                QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(userId).build()
                );

        return reverseIndex.query(r -> r
                .queryConditional(condition)
        ).iterator();
    }

    /// people who follow this user
    public List<Follow> getFollowers(String userId, int limit) {

        QueryConditional condition =
                QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(userId)
                                .build()
                );

        Page<Follow> page =
                reverseIndex.query(r -> r
                        .queryConditional(condition)
                        .limit(limit)
                ).iterator().next();

        return page.items();
    }

    /// people this user follows
    public List<Follow> getFollowing(String userId, int limit) {

        QueryConditional condition =
                QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(userId)
                                .build()
                );

        Page<Follow> page =
                followTable.query(r -> r
                        .queryConditional(condition)
                        .limit(limit)
                ).iterator().next();

        return page.items();
    }
}
