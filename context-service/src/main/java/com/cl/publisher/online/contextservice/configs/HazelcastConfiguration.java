package com.cl.publisher.online.contextservice.configs;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfiguration {
    @Bean
    public Config hazelCastConfig() {
        Config config = new Config();
        config.getIntegrityCheckerConfig().setEnabled(true);
        config.getJetConfig().setEnabled(true);
        config.setInstanceName("tpa-cdc-instance")
                .addMapConfig(
                        new MapConfig()
                                .setName("configuration-tpa-cdc")
                                .setTimeToLiveSeconds(-1));
        return config;
    }
}
