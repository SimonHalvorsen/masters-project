package com.mastersproject.alarmservice.kafkaConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class Consumer {

    public void run(){
        Logger logger = LoggerFactory.getLogger(Consumer.class.getName());

        String bootstrapServer = "35.233.35.208:9092";
        String groupId = "test";
        String topic = "sensor_data";

        // Latch for dealing with multiple threads
        CountDownLatch latch = new CountDownLatch(1);

        // Create consumer runnable
        logger.info("Creating the consumer thread");
        ConsumerRunnable myConsumerRunnable = new ConsumerRunnable(topic, bootstrapServer, groupId, latch);

        // Start the thread
        Thread myThread = new Thread(myConsumerRunnable);
        myThread.start();

        // add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            logger.info("Caught shutdown hook");
            myConsumerRunnable.shutdown();

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("Application has exited");
        }
        ));

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application got interrupted", e);
        } finally {
            logger.info("Application is closing");
        }
    }

}
