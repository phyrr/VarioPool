package com.variopool.dashboard.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "variopool.dashboard")
public class DashboardProperties {

    private Auth auth = new Auth();
    private Nacos nacos = new Nacos();
    private Redis redis = new Redis();
    private List<AppConfig> apps = new ArrayList<>();

    @Data
    public static class Auth {
        private String username = "admin";
        private String password = "admin";
    }

    @Data
    public static class Nacos {
        private String serverAddr = "127.0.0.1:8848";
    }

    @Data
    public static class Redis {
        private String configKey = "variopool:config:variopool-example";
        private String topic = "variopool:config:refresh:variopool-example";
    }

    @Data
    public static class AppConfig {
        private String name;
        private String dataId;
        private String group = "DEFAULT_GROUP";
        private String agentUrl;
    }
}
