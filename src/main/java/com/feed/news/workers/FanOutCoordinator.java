package com.feed.news.workers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.feed.news.domain.Follow;
import com.feed.news.events.FanOutChunkEvent;
import com.feed.news.events.PostCreatedEvent;
import com.feed.news.repository.FollowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.enhanced.dynamodb.model.Page;

@Component
public class FanOutCoordinator {

    private static final int CHUNK_SIZE = 500;

    private final FollowRepository followRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String chunkTopic;

    public FanOutCoordinator(
            FollowRepository followRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${feed.topics.fanout-chunk}") String chunkTopic) {
        this.followRepository = followRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.chunkTopic = chunkTopic;
    }

    @KafkaListener(topics = "${feed.topics.post-created}")
    public void handle(String message) throws Exception {
        PostCreatedEvent event =
                objectMapper.readValue(message, PostCreatedEvent.class);

        Iterator<Page<Follow>> pages =
                followRepository.iterateFollowersOf(event.getCreatorId());

        List<String> buffer = new ArrayList<>(CHUNK_SIZE);

        while (pages.hasNext()) {
            for (Follow f : pages.next().items()) {
                buffer.add(f.getUserFollowing());

                if (buffer.size() == CHUNK_SIZE) {
                    publishChunk(event, buffer);
                    buffer.clear();
                }
            }
        }

        if (!buffer.isEmpty()) {
            publishChunk(event, buffer);
        }
    }

    private void publishChunk(PostCreatedEvent event, List<String> followerIds)
            throws Exception {

        FanOutChunkEvent chunk =
                new FanOutChunkEvent(
                        event.getPostId(),
                        event.getCreatorId(),
                        event.getCreatedAt(),
                        event.getEventTime(),
                        List.copyOf(followerIds)
                );

        String payload = objectMapper.writeValueAsString(chunk);

        kafkaTemplate.send(
                chunkTopic,
                event.getPostId(),
                payload
        );
    }
}
