package com.abc.pricing.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic productPriceTopic() {
        return TopicBuilder.name("product-prices")
                .partitions(3) // Multiple partitions for scalability
                .replicas(1)
                .build();
    }
}