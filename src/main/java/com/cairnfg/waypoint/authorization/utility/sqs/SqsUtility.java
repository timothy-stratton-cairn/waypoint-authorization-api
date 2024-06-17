package com.cairnfg.waypoint.authorization.utility.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class SqsUtility {

  private final AmazonSQS sqsClient;

  private final ObjectMapper mapper = new ObjectMapper();

  public SqsUtility(AmazonSQS sqsClient) {
    this.sqsClient = sqsClient;
  }

  public void sendMessage(Object messageBody) throws JsonProcessingException {
    SendMessageRequest sendMessageRequest = new SendMessageRequest()
        .withQueueUrl("https://sqs.us-east-1.amazonaws.com/471112975273/dev-test-queue")
        .withMessageBody(mapper.writeValueAsString(messageBody));

    sqsClient.sendMessage(sendMessageRequest);
  }
}
