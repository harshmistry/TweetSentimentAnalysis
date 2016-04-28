package com.nyu.tweettrend;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;

public class SNSHelper {

	private static AWSCredentials credentials;
	private static AmazonSNSClient snsClient;
	private static String topicArn;
	private static String endpoint;
	
	public static void init(){
		
		
		snsClient = new AmazonSNSClient(AWSCredentialHelper.getCredentials());
		snsClient.setRegion(Region.getRegion(Regions.US_WEST_2));
		
		//create a new SNS topic
		CreateTopicRequest createTopicRequest = new CreateTopicRequest("SentimentEval");
		CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);
		
		//save TopicArn
		System.out.println(createTopicResult);
		topicArn = createTopicResult.getTopicArn();
		
		//get request id for CreateTopicRequest from SNS metadata		
		System.out.println("CreateTopicRequest - " + snsClient.getCachedResponseMetadata(createTopicRequest));
		
		
	}

	
	public static void subscribe(){
		
		SubscribeRequest subRequest = new SubscribeRequest(topicArn, "http", endpoint);
		snsClient.subscribe(subRequest);
		//get request id for SubscribeRequest from SNS metadata
		System.out.println("SubscribeRequest - " + snsClient.getCachedResponseMetadata(subRequest));
	}
	
	public static void publishMsg(String msg){
		PublishRequest publishRequest = new PublishRequest(topicArn, msg);
		PublishResult publishResult = snsClient.publish(publishRequest);
		//print MessageId of message published to SNS topic
		System.out.println("MessageId - " + publishResult.getMessageId());
	}
	
	public static void deleteTopic(){
		DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest(topicArn);
		snsClient.deleteTopic(deleteTopicRequest);
		//get request id for DeleteTopicRequest from SNS metadata
		System.out.println("DeleteTopicRequest - " + snsClient.getCachedResponseMetadata(deleteTopicRequest));
	
	
	public void confirmTopicSubmission(SNSMessage message) {
		ConfirmSubscriptionRequest confirmSubscriptionRequest = new ConfirmSubscriptionRequest()
		 							.withTopicArn(message.getTopicArn())
									.withToken(message.getToken());
		ConfirmSubscriptionResult resutlt = amazonSNSClient.confirmSubscription(confirmSubscriptionRequest);
		System.out.println("subscribed to " + resutlt.getSubscriptionArn());
		
	}
	
	
	
}
