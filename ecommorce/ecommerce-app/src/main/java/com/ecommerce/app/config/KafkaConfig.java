package com.ecommerce.app.config;

import com.ecommerce.shared.domain.event.DomainEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka topics and producer configuration.
 * Topics are auto-created if they don't exist (dev-friendly).
 * In production, manage topics via Terraform/Helm.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    // ------------------------------------------------------------------ topics

    @Bean public NewTopic orderPlacedTopic()    { return topic("order.placed"); }
    @Bean public NewTopic orderConfirmedTopic() { return topic("order.confirmed"); }
    @Bean public NewTopic orderShippedTopic()   { return topic("order.shipped"); }
    @Bean public NewTopic orderCancelledTopic() { return topic("order.cancelled"); }
    @Bean public NewTopic paymentCompletedTopic(){ return topic("payment.completed"); }
    @Bean public NewTopic paymentFailedTopic()  { return topic("payment.failed"); }
    @Bean public NewTopic customerRegisteredTopic(){ return topic("customer.registered"); }
    @Bean public NewTopic stockReservedTopic()  { return topic("inventory.stock.reserved"); }
    @Bean public NewTopic stockReleasedTopic()  { return topic("inventory.stock.released"); }

    // ------------------------------------------------------------------ producer

    @Bean
    public ProducerFactory<String, DomainEvent> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, DomainEvent> kafkaTemplate(
            ProducerFactory<String, DomainEvent> pf) {
        return new KafkaTemplate<>(pf);
    }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }
}
