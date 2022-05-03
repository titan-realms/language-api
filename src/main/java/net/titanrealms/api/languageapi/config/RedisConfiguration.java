package net.titanrealms.api.languageapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
public class RedisConfiguration {

    @Bean
    public Jedis jedis() {
        return new Jedis();
    }
}
