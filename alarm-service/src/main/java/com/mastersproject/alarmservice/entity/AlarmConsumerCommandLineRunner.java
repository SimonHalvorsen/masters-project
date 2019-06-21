package com.mastersproject.alarmservice.entity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;


@Component
public class AlarmConsumerCommandLineRunner implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(AlarmConsumerCommandLineRunner.class.getName());

    @Autowired
    AlarmRepository alarmRepository;

    private JsonParser jsonParser = new JsonParser();
    private String bootstrapServer = "34.77.67.20:9092";
    private String groupId = "alarm_service";
    private String resetConfig = "earliest";
    private String topic = "sensor_data";


    @Override
    public void run(String... args) throws Exception {
        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, resetConfig);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // Subscribe consumer to our topic(s)
        // Arrays.asList(t1, t2, t3...); to sub to multiple topics
        consumer.subscribe(Collections.singleton(topic));

        // Poll for new data
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    logger.info(record.toString());
                    String recordAsString = record.value()
                            .substring(1, record.value().length() - 1).replace("\\", "");
                    JsonObject recordAsJsonObject = jsonParser.parse(recordAsString).getAsJsonObject();

                    long sensorValue = recordAsJsonObject.get("new_value").getAsLong();

                    if (sensorValue <= 0) {

                        long facilityId = recordAsJsonObject.get("facility_id").getAsLong();
                        long sensorId = recordAsJsonObject.get("sensor_id").getAsLong();

                        Alarm alarm = new Alarm();
                        alarm.setFacilityId(facilityId);
                        alarm.setSensorId(sensorId);
                        logger.info(alarm.getFacilityId() + " LERJGNERJGNELRSDFJGNEROJGNFEORINVOIFDNVERJFVN " + alarm.getSensorId());

                        alarmRepository.save(alarm);
                        logger.info("ALARM PERSISTED: " + recordAsString + "  DOFGHSIERUDFLGHLEARIODSHFVODUFVHASØDIFVHDF");

                        logger.info(alarmRepository.findBySensorId(sensorId).toString() + " was retrieved from the database");
//                        producer.publishAlarm("alarms", record.value());
                    } else {
                        logger.info("Key: " + record.key() + ", Value: " + record.value());
                        logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                    }
                }
            }
        } catch (WakeupException e) {
            logger.info("Received shutdown signal!");
        } finally {
            consumer.close();
        }
    }
}
