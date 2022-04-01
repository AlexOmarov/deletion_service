package ru.somarov.deletion_service.util;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;
import org.springframework.kafka.support.converter.MessagingMessageConverter;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomKafkaTestUtils {

    public static final Logger log = LoggerFactory.getLogger(CustomKafkaTestUtils.class);

    public static String getHeader(ConsumerRecord<?, ?> record, String header) {
        byte[] bytes = record.headers().lastHeader(header).value();
        return new String(bytes);
    }

    public static String jsonPath(ConsumerRecord<?, ?> record, String path) {
        try {
            return JsonPath.parse(record.value().toString()).read(path);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    public static KafkaTemplate<String, String> createKafkaTemplateForJson(EmbeddedKafkaBroker kafkaEmbedded, String defaultTopic) {
        Map<String, Object> senderProps = KafkaTestUtils.producerProps(kafkaEmbedded.getBrokersAsString());
        senderProps.put("value.serializer", StringSerializer.class);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(senderProps);
        MessagingMessageConverter messageConverter = new MessagingMessageConverter();
        messageConverter.setHeaderMapper(new DefaultKafkaHeaderMapper());
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(pf);
        kafkaTemplate.setMessageConverter(messageConverter);
        if(defaultTopic != null) {
            kafkaTemplate.setDefaultTopic(defaultTopic);
        }
        return kafkaTemplate;
    }


    public static Consumer<String, String> createConsumerForJsonTopic(EmbeddedKafkaBroker kafkaEmbedded, String topic) {
        return createConsumerForJsonTopic(kafkaEmbedded, topic, UUID.randomUUID().toString(), "earliest");
    }

    public static Consumer<String, String> createConsumerForJsonTopic(EmbeddedKafkaBroker kafkaEmbedded, String topic,
                                                                      String consumerGroup, String autoOffsetReset) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(consumerGroup, "true", kafkaEmbedded);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<String, String> consumer = cf.createConsumer();
        kafkaEmbedded.consumeFromAnEmbeddedTopic(consumer, topic);
        consumer.poll(Duration.ZERO);
        return consumer;
    }

    public static Consumer<String, String> initConsumer(EmbeddedKafkaBroker embeddedKafka,
                                                        KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
                                                        List<String> topics, Integer partitions) throws InterruptedException {
        String consumerId = UUID.randomUUID().toString();
        Map<String, Object> configs = KafkaTestUtils.consumerProps("consumer-" + consumerId, "false", embeddedKafka);
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(),
                new StringDeserializer()).createConsumer();
        consumer.subscribe(topics);
        consumer.poll(Duration.ofMillis(0)); //wait for subscription
        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry.getListenerContainers()) {
            // here we have only real consumer, not the test one. Actually, this code should be moved out to
            // another method
            ContainerTestUtils.waitForAssignment(messageListenerContainer, 3);
        }
        return consumer;
    }

    public static KafkaTemplate<String, String> initProducer(EmbeddedKafkaBroker embeddedKafka, String topic) {
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(
                KafkaTestUtils.producerProps(embeddedKafka), new StringSerializer(), new StringSerializer());
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setDefaultTopic(topic);
        return kafkaTemplate;
    }

    /**
     * Cleans and unsubscribe, use in cleanup faze
     * @param consumer
     */
    public static void unsubscribe(Consumer<String, String> consumer) {
        log.info("Unsubscribe {}", consumer.listTopics().keySet());
        consumer.unsubscribe();
        consumer.close(Duration.of(1000, ChronoUnit.MILLIS));
    }

}
