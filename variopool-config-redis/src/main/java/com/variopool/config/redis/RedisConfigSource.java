package com.variopool.config.redis;

import com.variopool.core.config.ConfigSource;
import com.variopool.core.model.VarioPoolConfig;
import com.variopool.core.refresh.ThreadPoolRefreshService;
import com.variopool.spring.autoconfigure.VarioPoolProperties;
import com.variopool.spring.event.ConfigRefreshedEvent;
import com.variopool.spring.support.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

@Slf4j
public class RedisConfigSource implements ConfigSource, MessageListener<RedisRefreshMessage> {

    private final RedissonClient redissonClient;
    private final VarioPoolProperties properties;
    private final ThreadPoolRefreshService refreshService;

    public RedisConfigSource(RedissonClient redissonClient,
                             VarioPoolProperties properties,
                             ThreadPoolRefreshService refreshService) {
        this.redissonClient = redissonClient;
        this.properties = properties;
        this.refreshService = refreshService;
    }

    @Override
    public void start() {
        VarioPoolConfig.RedisConfig redis = properties.getRedis();
        if (redis == null || redis.getConfigKey() == null) {
            log.warn("VarioPool redis config is missing, skip redis listener");
            return;
        }
        RTopic topic = redissonClient.getTopic(resolveTopic(redis));
        topic.addListener(RedisRefreshMessage.class, this);
        log.info("VarioPool redis listener registered, key={}, topic={}", redis.getConfigKey(), resolveTopic(redis));

        String initial = load();
        if (initial != null && !initial.isBlank()) {
            publish(initial);
        }
    }

    @Override
    public String load() {
        VarioPoolConfig.RedisConfig redis = properties.getRedis();
        if (redis == null || redis.getConfigKey() == null) {
            return null;
        }
        RBucket<String> bucket = redissonClient.getBucket(redis.getConfigKey());
        return bucket.get();
    }

    @Override
    public void onMessage(CharSequence channel, RedisRefreshMessage message) {
        log.info("VarioPool redis refresh signal received from {}", channel);
        String content = load();
        if (content == null || content.isBlank()) {
            log.warn("VarioPool redis config key is empty: {}", properties.getRedis().getConfigKey());
            return;
        }
        publish(content);
    }

    public void publishRefreshSignal() {
        redissonClient.getTopic(resolveTopic(properties.getRedis()))
                .publish(new RedisRefreshMessage("REFRESH"));
    }

    private void publish(String content) {
        VarioPoolConfig config = refreshService.parse(content);
        ApplicationContextHolder.publishEvent(new ConfigRefreshedEvent(this, content, config));
    }

    private String resolveTopic(VarioPoolConfig.RedisConfig redis) {
        if (redis.getTopic() != null && !redis.getTopic().isBlank()) {
            return redis.getTopic();
        }
        return "variopool:config:refresh:" + redis.getConfigKey();
    }
}
