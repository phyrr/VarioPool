package com.variopool.config.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Redis pub/sub message for configuration refresh.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisRefreshMessage {
    private String action = "REFRESH";
}
