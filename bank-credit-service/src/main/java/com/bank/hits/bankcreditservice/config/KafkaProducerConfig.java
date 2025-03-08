package com.bank.hits.bankcreditservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    @Bean
    public NewTopic userAccountCreateRequest() {
        return new NewTopic("credit.client.info.request", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountCreateRequest2() {
        return new NewTopic("credit.client.info.response", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountCreateRequest3() {
        return new NewTopic("credit.approved", 1, (short) 1);
    }
    @Bean
    public NewTopic userAccountCreateRequest4() {
        return new NewTopic("credit.payment.request", 1, (short) 1);
    }
    @Bean
    public NewTopic userAccountCreateRequest5() {
        return new NewTopic("credit.payment.response", 1, (short) 1);
    }
}
