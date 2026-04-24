package com.internship.backend_assignment.schedular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSweeper {

    private final StringRedisTemplate redisTemplate;

    @Scheduled(fixedRate =1000*60*5)
    public void sweepNotifications() {
        Set<String> keys = redisTemplate.keys("user:*:pending_notifications");

        if ( keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                String userId = key.split(":")[1];

                List<String> messages = redisTemplate.opsForList().range(key, 0, -1);
                redisTemplate.delete(key);

                if (messages != null && !messages.isEmpty()) {
                    String firstBotMsg = messages.getFirst();
                    int otherCount = messages.size() - 1;

                    log.info("Summarized Push Notification for User {}: {} and [{}] others interacted with your posts.", userId, firstBotMsg, otherCount);
                }
            }
        }
    }
}