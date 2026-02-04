package com.feed.news.workers;

import com.feed.news.domain.PrecomputedFeed;
import com.feed.news.events.FanOutChunkEvent;
import com.feed.news.repository.PrecomputedFeedRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FanOutChunkWorker {

    private final PrecomputedFeedRepository feedRepository;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public FanOutChunkWorker(
            PrecomputedFeedRepository feedRepository,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry) {
        this.feedRepository = feedRepository;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    @KafkaListener(topics = "${feed.topics.fanout-chunk}")
    public void handle(String message) throws Exception {
        try {
            FanOutChunkEvent event =
                    objectMapper.readValue(message, FanOutChunkEvent.class);

            MDC.put("postId", event.getPostId());
            MDC.put("creatorId", event.getCreatorId());
            MDC.put("eventTime", String.valueOf(event.getEventTime()));

            log.info("Processing fanout chunk");

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

                if (event.getEventTime() > 0) {
                    long latencyMillis =
                            System.currentTimeMillis() - event.getEventTime();

                    Timer.builder("feed.propagation.seconds")
                            .description("End-to-end feed propagation latency")
                            .tag("source", "fanout")
                            .tag("result", "success")
                            .register(meterRegistry)
                            .record(latencyMillis, TimeUnit.MILLISECONDS);
                }
            }
        }
        finally {
            MDC.clear();
        }
    }
}
