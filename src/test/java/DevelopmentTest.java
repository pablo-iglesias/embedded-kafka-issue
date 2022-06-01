import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ExtendWith({ SpringExtension.class })
@WebAppConfiguration
@ContextConfiguration(classes = { MockMvcConfiguration.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, topics = {"input-topic", "output-topic"})
@Import({KafkaConfig.class})
public class DevelopmentTest {

    private final Logger log = LoggerFactory.getLogger(DevelopmentTest.class);

    @Autowired
    KafkaTemplate<String, String> producer;

    @Autowired
    Consumer<String, String> consumer;

    @Test
    public void test() {
        ProducerRecord<String, String> message = new ProducerRecord<>("input-topic", 0,
                System.currentTimeMillis(), "Key", "One two three three");
        producer.send(message);
        consumer.subscribe(List.of("output-topic"));
        int timer = 0;
        while (timer < 1000) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
            consumerRecords.forEach(r -> log.info("Received key {} value {}", r.key(), r.value()));
            timer += 100;
        }
    }
}