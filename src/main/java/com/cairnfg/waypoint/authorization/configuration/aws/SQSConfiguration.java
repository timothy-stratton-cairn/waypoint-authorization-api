package com.cairnfg.waypoint.authorization.configuration.aws;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SQSConfiguration {

  @Bean
  public AmazonSQS sqsClient() {
    return AmazonSQSClient.builder()
        .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
        .withRegion(Regions.US_EAST_1)
        .build();
  }
}
