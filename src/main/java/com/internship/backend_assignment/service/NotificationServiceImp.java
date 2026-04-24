package com.internship.backend_assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImp implements NotificationService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void processBotNotification(Long humanUserId, Long botId) {

        String cooldownKey = "notification_cooldown:" + humanUserId;
        String listKey = "user:" + humanUserId + ":pending_notifications";
        String message = "Bot " + botId + " replied to your post";

        boolean hasCooldown =redisTemplate.hasKey(cooldownKey);

        if (Boolean.TRUE.equals(hasCooldown)) {
            redisTemplate.opsForList().rightPush(listKey, message);
        }

        else {
            log.info("Push Notification Sent to User " + humanUserId + ": " + message);
            redisTemplate.opsForValue().set(cooldownKey, "active", Duration.ofMinutes(15));
        }


    }
}