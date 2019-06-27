package com.mastersproject.alarmservice.kafkaProducer;

import com.mastersproject.alarmservice.entity.Alarm;
import com.mastersproject.alarmservice.entity.AlarmRepository;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

@Configuration
@EnableScheduling
@Component
public class AlarmProducerWithCallback {

    final Logger logger = LoggerFactory.getLogger(AlarmProducerWithCallback.class);
    @Autowired
    AlarmRepository alarmRepository;

    public AlarmProducerWithCallback() {

    }

//        public void publishAlarm(String topic, String value) {
    @Scheduled(fixedDelay = 10000)
    public void publishAlarm(){

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "34.77.67.20:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        //leader will wait for in-sync replicas to ack the record. Not necessary in our case since we only
        //have one broker (only leader)
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        //the number of unacknowledged requests the client will send before blocking
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

        // high throughput producer at the expense of a bit of latency and CPU usage
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));

        //create producer
        final KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
//        final ProducerRecord<String, String> record = new ProducerRecord<>(topic, value);
        List<Alarm> alarms = alarmRepository.findAllByActive(true);

        for (Alarm alarm : alarms) {

            final ProducerRecord<String, String> record = new ProducerRecord<>("alarms", alarm.toString());

            // send data async
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
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
                }
            });
        }

        // flush data
        producer.flush();

        // flush and close
        producer.close();
        }
}
