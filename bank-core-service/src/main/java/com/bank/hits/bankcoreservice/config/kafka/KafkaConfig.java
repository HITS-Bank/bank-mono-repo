package com.bank.hits.bankcoreservice.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    private final String BOOTSTRAP_SERVERS = "kafka:9092";

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        final Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Register JavaTimeModule
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional: ensures date is serialized as ISO-8601
        return objectMapper;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        final Map<String, Object> configProps = new HashMap<>();

        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "bank.group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        final ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public NewTopic userAccountCreateRequest() {
        return new NewTopic("user.account.create.request", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountCreateResponse() {
        return new NewTopic("user.account.create.response", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountCloseRequest() {
        return new NewTopic("user.account.close.request", 1, (short) 1);
    }

    @Bean
    public NewTopic creditApprovedRequest() {
        return new NewTopic("credit.approved.request", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountCloseResponse() {
        return new NewTopic("user.account.close.response", 1, (short) 1);
    }

    @Bean
    public NewTopic userCreditRepaymentRequest() {
        return new NewTopic("credit.repayment.request", 1, (short) 1);
    }

    @Bean
    public NewTopic userCreditRepaymentResponse() {
        return new NewTopic("credit.repayment.response", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountDepositRequest() {
        return new NewTopic("user.account.deposit.request", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountDepositResponse() {
        return new NewTopic("user.account.deposit.response", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountWithdrawRequest() {
        return new NewTopic("user.account.withdraw.request", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountWithdrawResponse() {
        return new NewTopic("user.account.withdraw.response", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountTransactionHistoryRequest() {
        return new NewTopic("user.account.transaction.history.request", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountTransactionHistoryResponse() {
        return new NewTopic("user.account.transaction.history.response", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountAllBlockRequest() {
        return new NewTopic("user.account.all.block.request", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountAllBlockResponse() {
        return new NewTopic("user.account.all.block.response", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountAllUnlockRequest() {
        return new NewTopic("user.account.all.unlock.request", 1, (short) 1);
    }

    @Bean
    public NewTopic userAccountAllUnlockResponse() {
        return new NewTopic("user.account.all.unlock.response", 1, (short) 1);
    }

    @Bean
    public NewTopic employeeViewAccountTransactionHistoryRequest() {
        return new NewTopic("employee.view.account.transaction.history.request", 1, (short) 1);
    }

    @Bean
    public NewTopic employeeViewAccountTransactionHistoryResponse() {
        return new NewTopic("employee.view.account.transaction.history.response", 1, (short) 1);
    }

    @Bean
    public NewTopic creditApprovedResponse() {
        return new NewTopic("credit.approved.response", 1, (short) 1);
    }

}
