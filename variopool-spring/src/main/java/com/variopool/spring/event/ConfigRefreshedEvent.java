package com.variopool.spring.event;

import com.variopool.core.model.VarioPoolConfig;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ConfigRefreshedEvent extends ApplicationEvent {

    private final String rawContent;
    private final VarioPoolConfig config;

    public ConfigRefreshedEvent(Object source, String rawContent, VarioPoolConfig config) {
        super(source);
        this.rawContent = rawContent;
        this.config = config;
    }
}
