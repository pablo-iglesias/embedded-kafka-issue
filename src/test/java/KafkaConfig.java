import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {

    @Autowired
    private EmbeddedKafkaBroker broker;

    public String getBootstrapServers() {
        return "listeners=PLAINTEXT://" + broker.getBrokersAsString();
    }

    public ConsumerFactory<String, String> getConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "TestConsumerGroup");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public Consumer<String, String> consumer() {
        return getConsumerFactory().createConsumer();
    }

    @Bean
    public KafkaTemplate<String, String> producer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration getStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100L);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "WordCount");
        return new KafkaStreamsConfiguration(props);
    }

    @Bean(name = "INPUT")
    public NewTopic inputTopic() {
        return new NewTopic("input-topic", 1, (short) 1);
    }

    @Bean(name = "OUTPUT")
    public NewTopic outputTopic() {
        return new NewTopic("output-topic", 1, (short) 1);
    }

    @Bean
    StreamsBuilderFactoryBeanConfigurer config() {
        return fb -> {
            fb.setCleanupConfig(new CleanupConfig(true, true));
        };
    }
}