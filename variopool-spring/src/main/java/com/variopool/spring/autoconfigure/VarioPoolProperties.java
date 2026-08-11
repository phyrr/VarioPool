package com.variopool.spring.autoconfigure;

import com.variopool.core.model.VarioPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = VarioPoolConfig.PREFIX)
public class VarioPoolProperties extends VarioPoolConfig {
}
