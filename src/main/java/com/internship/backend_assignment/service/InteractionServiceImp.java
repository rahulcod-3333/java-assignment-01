package com.internship.backend_assignment.service;

import com.internship.backend_assignment.exception.TooManyRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionServiceImp implements InteractionService {
    private final StringRedisTemplate redisTemplate;
    private static final int POINTS_BOT_REPLY = 1;
    private static final int POINTS_HUMAN_LIKE = 20;
    private static final int POINTS_HUMAN_COMMENT = 50;

    private static final int MAX_BOT_REPLIES = 100;

    private static final long KEY_EXPIRATION_SECONDS = 3600*24*3; //3 days
    private static final String BOT_COUNT_SCRIPT =
            "local current = redis.call('GET', KEYS[1]) " +
                    "if current and tonumber(current) >= tonumber(ARGV[1]) then " +
                    "  return -1 " +
                    "else " +
                    "  local newVal = redis.call('INCR', KEYS[1]) " +
                    "  redis.call('EXPIRE', KEYS[1], ARGV[2]) " + // Set TTL instantly
                    "  return newVal " +
                    "end";

    @Override
    public void validateBotInteraction(Long postId, Long botId, Long humanAuthorId) {
        // cooldown cap:
        if (humanAuthorId != null) {
            String cooldownKey = "cooldown:bot_" + botId + ":human_" + humanAuthorId;

            Boolean isFirstInteraction = redisTemplate.opsForValue().setIfAbsent(cooldownKey, "active", Duration.ofMinutes(10));

            if (Boolean.FALSE.equals(isFirstInteraction)) {
                log.warn("bot {} hit cooldown for human {}", botId, humanAuthorId);
                throw new TooManyRequestException("Bot"+ botId + "already interacted with this user recently, try after 10 mins");
            }

        }
        // horizontal cap:
        String botCountKey = "post:" + postId + ":bot_count";
        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(BOT_COUNT_SCRIPT, Long.class),
                List.of(botCountKey),
                String.valueOf(MAX_BOT_REPLIES),
                String.valueOf(KEY_EXPIRATION_SECONDS)
        );

        if (result == null || result == -1L) {
            log.warn("post {} has hit the 100 bot reply cap, rejecting bot {}", postId, botId);
            throw new TooManyRequestException(
                    "Post " + postId + " has reached the maximum of 100 bot replies"
            );
        }

        log.info("bot {} allowed on post {}, current bot count: {}", botId, postId, result);
    }

    @Override
    public void incrementViralityScore(Long postId, String interactionType) {
        String viralityKey = "post:" + postId + ":virality_score";
        int points = pointsForInteraction(interactionType);

        redisTemplate.opsForValue().increment(viralityKey, points);
        redisTemplate.expire(viralityKey, Duration.ofSeconds(KEY_EXPIRATION_SECONDS));

        log.info("virality score updated for post {}, +{} points ({})", postId, points, interactionType);
    }
    @Override
    public int pointsForInteraction(String type) {

        return switch (type.toUpperCase()) {
            case "BOT_REPLY" -> POINTS_BOT_REPLY;
            case "HUMAN_LIKE" -> POINTS_HUMAN_LIKE;
            case "HUMAN_COMMENT" -> POINTS_HUMAN_COMMENT;
            default -> 0;

        };



    }
}
