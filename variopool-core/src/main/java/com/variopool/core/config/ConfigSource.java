package com.variopool.core.config;

/**
 * Abstraction for external configuration sources (Nacos, Redis, etc.).
 */
public interface ConfigSource {

    /**
     * Start listening for configuration changes.
     */
    void start();

    /**
     * Load the latest raw configuration text.
     */
    String load();
}
