/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.nyu.tweettrend;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import com.amazonaws.services.sqs.model.*;
/**
 * This sample demonstrates how to make basic requests to Amazon SQS using the
 * AWS SDK for Java.
 * <p>
 * <b>Prerequisites:</b> You must have a valid Amazon Web
 * Services developer account, and be signed up to use Amazon SQS. For more
 * information on Amazon SQS, see http://aws.amazon.com/sqs.
 * <p>
 * WANRNING:</b> To avoid accidental leakage of your credentials, DO NOT keep
 * the credentials file in your source directory.
 */
public class receiveSQS {

	public static void main(String[] args) throws Exception {

		/*
		 * The ProfileCredentialsProvider will return your [default]
		 * credential profile by reading from the credentials file located at
		 * ().
		 */

		BasicAWSCredentials credentials = new BasicAWSCredentials(YOUR_SECRET_KEY,YOUR_ACCESS_SECRET);
		try {
			//         // credentials = new ProfileCredentialsProvider("default").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
							"Please make sure that your credentials file is at the correct " +
							"location (/Users/daniel/.aws/credentials), and is in valid format.",
							e);
		}

		AmazonSQSAsync sqs = new AmazonSQSAsyncClient(credentials);
		AmazonSQSAsync bufferedSqs = new AmazonSQSBufferedAsyncClient(sqs);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		sqs.setRegion(usWest2);
		CreateQueueRequest createRequest = new CreateQueueRequest().withQueueName("demoq");
		CreateQueueResult res = bufferedSqs.createQueue(createRequest);
		 

		System.out.println("===========================================");
		System.out.println("Getting Messages From Amazon SQS");
		System.out.println("===========================================\n");

		try {

			// List queues
			System.out.println("Listing all queues in your account.\n");
//			for (String queueUrl : sqs.listQueues().getQueueUrls()) {
//				System.out.println("  QueueUrl: " + queueUrl);
				
				String queueUrl = "demoq";
				GetQueueAttributesRequest qar = new GetQueueAttributesRequest( queueUrl );
		         qar = qar.withAttributeNames("ApproximateNumberOfMessages");
		         //qar.setAttributeNames( Arrays.asList( ATTR_NAME ));
		         
		        // Map map = sqs.getQueueAttributes( qar) .getAttributes();
		         Map<String,String> map=sqs.getQueueAttributes(qar).getAttributes();
		         int totalmessage=Integer.parseInt(map.get("ApproximateNumberOfMessages"));
		         System.out.println("Number  = "+totalmessage);

				for(int i=0;;i++){
				// Receive messages
				//System.out.println("Receiving messages from MyQueue.\n");
				ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest().withMaxNumberOfMessages(1)
					    .withQueueUrl(queueUrl);
				ReceiveMessageResult rx = bufferedSqs.receiveMessage(receiveMessageRequest);
				
				//String url = sqs.createQueue( new CreateQueueRequest( queue )).getQueueUrl();
				 
		         
				
				List<Message> messages = rx.getMessages();
						//sqs.receiveMessage(receiveMessageRequest).getMessages();
				//System.out.println("Number of Messages : "+messages.size());
				
				
				for (Message message : messages) {
					
//					System.out.println("  Message");
//					System.out.println("    MessageId:     " + message.getMessageId());
//					System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
//					System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
//					System.out.println("    Body:          " + message.getBody());
					printmessage(message.getBody());
					for (Entry<String, String> entry : message.getAttributes().entrySet()) {
						System.out.println("  Attribute");
						System.out.println("    Name:  " + entry.getKey());
						System.out.println("    Value: " + entry.getValue());
					}
				} 
				}
					
//					// Delete a message
//					System.out.println("Deleting a message.\n");
//					String messageRecieptHandle = messages.get(0).getReceiptHandle();
//					sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle));
//
//					// Delete a queue
//					System.out.println("Deleting the test queue.\n");
//					sqs.deleteQueue(new DeleteQueueRequest(queueUrl));
					
				
//END FOR			}
		}
		catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it " +
					"to Amazon SQS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered " +
					"a serious internal problem while trying to communicate with SQS, such as not " +
					"being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}
	
	static void printmessage(String tweetmessage){
		String s[] = tweetmessage.split(",");
		String tweettext = s[0];
		String tweetid = s[1];
		String latitude = s[2];
		String longitude = s[3];
		String skey = s[4];
		String created = s[5];
		
		System.out.println("Tweet : "+tweettext+" --- Latitude --> "+latitude+" --- Longitude --> "+longitude);
	}
}
