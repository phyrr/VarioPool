package com.variopool.spring.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VarioPoolMarkerConfiguration {

    @Bean
    public Marker marker() {
        return new Marker();
    }

    public static class Marker {
    }
}
