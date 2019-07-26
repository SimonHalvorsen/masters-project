package com.mastersproject.alarmservice.kafkaProducer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastersproject.alarmservice.entity.Alarm;
import com.mastersproject.alarmservice.entity.AlarmRepository;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

@Component
public class AlarmProducerWithCallback {

    private final Logger logger = LoggerFactory.getLogger(AlarmProducerWithCallback.class);
    @Autowired
    AlarmRepository alarmRepository;
    @Autowired
    ObjectMapper mapper;

    private KafkaProducer<String, String> producer;

    public AlarmProducerWithCallback(){
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "34.77.67.20:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        //leader will wait for in-sync replicas to ack the record
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));

        // the number of unacknowledged requests the client will send before blocking
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

        // high throughput producer at the expense of a bit of latency and CPU usage
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "10");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));

        this.producer = new KafkaProducer<>(properties);
    }

    public void publishAlarm(){

        List<Alarm> alarms = alarmRepository.findActiveNotPublished(true, false);

        if (!alarms.isEmpty()) {
            for (Alarm alarm : alarms) {

                try {
                    String jsonString = mapper.writeValueAsString(alarm);

                    final ProducerRecord<String, String> record = new ProducerRecord<>("alarms", jsonString);

                    // send data async
                    producer.send(record, (recordMetadata, e) -> {
                        // exectues every time a record is successfully sent or an exception is thrown
                        if (e == null) {
                            // the record was succefully sent
                            logger.info("Received new metadata. \n" +
                                    "Topic: " + recordMetadata.topic() + "\n" +
                                    "Partition: " + recordMetadata.partition() + "\n" +
                                    "Offset: " + recordMetadata.offset() + "\n" +
                                    "Timestamp: " + recordMetadata.timestamp());
                        } else {
                            logger.error("Error while producing ", e);
                        }
                    });
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                alarm.setPublished(true);
                alarmRepository.save(alarm);
            }
        }
    }
}
