package com.mastersproject.alarmservice.consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mastersproject.alarmservice.configs.Config;
import com.mastersproject.alarmservice.entity.Alarm;
import com.mastersproject.alarmservice.entity.AlarmRepository;
import com.mastersproject.alarmservice.kafkaProducer.AlarmProducerWithCallback;
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
public class SensorDataConsumer implements CommandLineRunner {


    @Autowired
    AlarmRepository alarmRepository;
    @Autowired
    AlarmProducerWithCallback producer;

    private Logger logger = LoggerFactory.getLogger(SensorDataConsumer.class.getName());
    private JsonParser jsonParser = new JsonParser();
    private String groupId = "alarm_service";
    private String resetConfig = "latest";
    private String topic = "sensor_data";
    private String bootstrapServer = new Config().getIp();


    @Override
    public void run(String... args) throws Exception {

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, resetConfig);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // Arrays.asList(t1, t2, t3...); to sub to multiple topics
        consumer.subscribe(Collections.singleton(topic));

        try {
            while (true) {
                producer.publishAlarm();

                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    logger.info(record.toString());
                    String recordAsString = record.value()
                            .substring(1, record.value().length() - 1).replace("\\", "");
                    JsonObject recordAsJsonObject = jsonParser.parse(recordAsString).getAsJsonObject();

                    long sensorValue = recordAsJsonObject.get("new_value").getAsLong();

                    if (sensorValue <= 0) {

                        long sensorId = recordAsJsonObject.get("sensor_id").getAsLong();

                        Alarm dbLookup = alarmRepository.findBySensorId(sensorId);

                        if(dbLookup != null && !dbLookup.isActive()){
                            dbLookup.setActive(true);
                            alarmRepository.save(dbLookup);
                            logger.info("Status of sensor with id " + sensorId + " set to active");
                        }
                        else if(dbLookup == null) {
                            long facilityId = recordAsJsonObject.get("facility_id").getAsLong();
                            double timestamp = recordAsJsonObject.get("timestamp").getAsDouble();
                            alarmRepository.save(new Alarm(facilityId, sensorId, true, timestamp, false));
                            logger.info("Alarm from sensor with id " + sensorId + " and timestamp " + timestamp + " persisted");
                        }
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
