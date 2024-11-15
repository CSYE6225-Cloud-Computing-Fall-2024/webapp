package com.swamyms.webapp.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

@Service
public class SNSService {

    private final SnsClient snsClient;
    private final Logger logger = LoggerFactory.getLogger(FileService.class); // Logger instance
    private final MeterRegistry meterRegistry; // Meter registry for metrics

    private final Timer dbQueryTimer; // Timer for authenticateUser method

    public SNSService(SnsClient theSnsClient, MeterRegistry meterRegistry) {
        this.snsClient = theSnsClient;
        this.meterRegistry = meterRegistry;
        this.dbQueryTimer = Timer.builder("db.SNSService.queries.execution")
                .description("Time taken for User database queries")
                .register(meterRegistry);
    }

    public void publishToSNS(String topicArn, String message) {
        logger.info("Sns Message forwarded to lambda function:{}", message);
        long startTime = System.currentTimeMillis(); // Start timing API call
        logger.info("Publishing Message to SNS: {}", topicArn); // Log saving user
        Timer.Sample sample = Timer.start(meterRegistry); // Start timing
        try {
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(message)
                    .build();
            PublishResponse publishResponse = snsClient.publish(publishRequest);

            // Log the message ID if successful
            logger.info("Message published to SNS with ID: {}", publishResponse.messageId());
            sample.stop(dbQueryTimer); // Stop timing and record
            logger.info("Publishing Message to SNS Time taken: {} ms", System.currentTimeMillis() - startTime); // Log info
        } catch (SnsException e) {
            // Log error with SNS-specific details
            logger.error("Failed to publish message to SNS. Error Message: {}. Status Code: {}. Request ID: {}",
                    e.awsErrorDetails().errorMessage(),
                    e.statusCode(),
                    e.requestId());
        } catch (Exception e) {
            // Log any unexpected errors
            logger.error("An unexpected error occurred while publishing message to SNS: ", e);
        }
    }
}
