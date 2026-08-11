package com.variopool.spring.listener;

import com.variopool.core.refresh.ThreadPoolRefreshService;
import com.variopool.spring.event.ConfigRefreshedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;

@RequiredArgsConstructor
public class ConfigRefreshListener {

    private final ThreadPoolRefreshService refreshService;

    @EventListener
    public void onConfigRefreshed(ConfigRefreshedEvent event) {
        refreshService.refresh(event.getRawContent());
    }
}
