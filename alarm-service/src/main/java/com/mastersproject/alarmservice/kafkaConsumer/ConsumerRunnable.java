package com.mastersproject.alarmservice.kafkaConsumer;


import com.google.gson.JsonParser;
import com.mastersproject.alarmservice.kafkaProducer.ProducerWithCallback;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerRunnable implements Runnable{

    private CountDownLatch latch;
    private KafkaConsumer<String, String> consumer;
    private Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class.getName());
    private static JsonParser jsonParser = new JsonParser();
    private ProducerWithCallback producer = new ProducerWithCallback();

    public ConsumerRunnable(String topic, String bootstrapServer, String groupId, CountDownLatch latch){
        this.latch = latch;

        // Create consumer configs
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Create consumer
        consumer = new KafkaConsumer<String, String>(properties);

        // Subscribe consumer to our topic(s)
        // Arrays.asList(t1, t2, t3...); to sub to multiple topics
        consumer.subscribe(Collections.singleton(topic));
    }

    @Override
    public void run() {

        // Poll for new data
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    String sensorValueString = record.value().substring(1, record.value().length()-1).replace("\\", "");
                    float sensorValue = jsonParser.parse(sensorValueString)
                            .getAsJsonObject()
                            .get("new_value")
                            .getAsFloat();

                    if (sensorValue <= 0) {
                        producer.publishAlarm("alarms", record.value());
                    } else {
                        logger.info("Key: " + record.key() + ", Value: " + record.value());
                        logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());

                    }
                }
            }
        } catch (WakeupException e) {
            logger.info("Received shutdown signal!");
        }
        finally {
            consumer.close();
            // Tell our main code that we are done with the consumer
            latch.countDown();
        }
    }

    public void shutdown(){
        // The wakeup() method is a special method to interrupt consumer.poll()
        // it will throw the exception WakeupException
        consumer.wakeup();
    }

}
