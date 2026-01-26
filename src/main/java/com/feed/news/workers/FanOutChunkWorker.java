package com.feed.news.workers;

import com.feed.news.domain.PrecomputedFeed;
import com.feed.news.events.FanOutChunkEvent;
import com.feed.news.repository.PrecomputedFeedRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FanOutChunkWorker {

    private final PrecomputedFeedRepository feedRepository;
    private final ObjectMapper objectMapper;

    public FanOutChunkWorker(
            PrecomputedFeedRepository feedRepository,
            ObjectMapper objectMapper) {
        this.feedRepository = feedRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${feed.topics.fanout-chunk}")
    public void handle(String message) throws Exception {

        FanOutChunkEvent event =
                objectMapper.readValue(message, FanOutChunkEvent.class);

        for (String followerId : event.getFollowerIds()) {

            PrecomputedFeed feedItem = new PrecomputedFeed();
            feedItem.setUserId(followerId);
            feedItem.setPostId(event.getPostId());
            feedItem.setCreatorId(event.getCreatorId());
            feedItem.setSortKey(
                    event.getCreatedAt().toEpochMilli()
                            + "#" + event.getPostId()
            );

            // Idempotent write
            feedRepository.putIfAbsent(feedItem);
        }
    }
}
