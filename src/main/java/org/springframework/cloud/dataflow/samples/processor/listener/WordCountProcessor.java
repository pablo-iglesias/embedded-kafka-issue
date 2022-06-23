package org.springframework.cloud.dataflow.samples.processor.listener;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class WordCountProcessor {

    @Autowired
    public void buildPipeline(StreamsBuilder builder) {
        KStream<String, String> stream = builder.addStateStore(
            Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore("count"), Serdes.String(), Serdes.String()))
            .stream("input-topic");
        stream
            .peek((k, v) -> System.out.println("k:" + k + ", v:" + v))
            .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
            .peek((k, v) -> System.out.println("k:" + k + ", v:" + v))
            .groupBy((key, word) -> word, Grouped.with(Serdes.String(), Serdes.String()))
            .count().toStream()
            .peek((k, v) -> System.out.println("k:" + k + ", v:" + v))
            .to("output-topic");
    }
}
