package com.mastersproject.alarmservice.kafkaProducer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class ProducerWithCallback {

    final Logger logger = LoggerFactory.getLogger(ProducerWithCallback.class);

    public ProducerWithCallback() {

    }

        public void publishAlarm(String topic, String value) {

            Properties properties = new Properties();
            String bootstrapServer = "35.233.35.208:9092";
            properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
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
            final ProducerRecord<String, String> record = new ProducerRecord<>(topic, value);

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

            // flush data
            producer.flush();

            // flush and close
            producer.close();
        }
}
