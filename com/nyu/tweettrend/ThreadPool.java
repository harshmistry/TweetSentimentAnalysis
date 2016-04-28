package com.nyu.tweettrend;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.alchemyapi.api.AlchemyAPI;

public class ThreadPool {
	
	
	public static void main(String args[]) {
				
		
		String[] tweetPool = {"Kushal is Clever", "Sau is a fool","Harsh is Awesome","Nitin is Cool","Swar is tensed","Sau is an asshole","Peaceful Warrior","Makes me mad","Be nice to me","Hello, How are you?","That movie was Great"}; 
		int len = tweetPool.length;
		ExecutorService service = Executors.newFixedThreadPool(10);
		for (int i =0; ; i++){
			service.submit(new Task());
		}
	}
	
	 // utility method
    public static String getStringFromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);

            return writer.toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

final class Task implements Runnable{
	
	static String queueUrl = "demoq";
	static AmazonSQSAsync sqs;
	static AmazonSQSAsync bufferedSqs;
	static Region usWest2;
	static CreateQueueRequest createRequest;
	static CreateQueueResult res ;
	static AmazonSQSClient sqsclient;
	static String myQueue;
	static String myQueue1;
	
	static{
	
		 //*******************************************************************************//
	      //							SQS  CODE										   //
	     //*******************************************************************************//
	      		
				//BasicAWSCredentials credentials=null;
				AWSCredentials credentials=null;
				try {
					System.out.println("Created credentials:"+ReadPropertiesFile.getInstance("AwsCredentials.properties").getKey("secretKey")+"--"+ReadPropertiesFile.getInstance("AwsCredentials.properties").getKey("accessKey"));
					//credentials = new BasicAWSCredentials(ReadPropertiesFile.getInstance("AwsCredentials.properties").getKey("secretKey"),ReadPropertiesFile.getInstance("AwsCredentials.properties").getKey("accessKey"));
					credentials=new ProfileCredentialsProvider("default").getCredentials();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					System.out.println("IOException");
					e1.printStackTrace();
				}
				catch (Exception e) {
					throw new AmazonClientException(
							"Cannot load the credentials from the credential profiles file. " +
									"Please make sure that your credentials file is at the correct " +
									"location (/Users/harsh/.aws/credentials), and is in valid format.",
									e);
				}
				
				try {
				sqs = new AmazonSQSAsyncClient(credentials);
				bufferedSqs = new AmazonSQSBufferedAsyncClient(sqs);
				usWest2 = Region.getRegion(Regions.US_WEST_2);
				sqs.setRegion(usWest2);
				createRequest = new CreateQueueRequest().withQueueName("demoq");
				res = bufferedSqs.createQueue(createRequest);
	
	
				System.out.println("===========================================");
				System.out.println("Getting Messages From Amazon SQS");
				System.out.println("===========================================\n");   
				
				// List queues
				System.out.println("Queue Name : MyQueue "+queueUrl);
		       		
				// Create Response queue
					sqsInit(credentials);
				}
				
				catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("SQS exception");
					e.printStackTrace();
				}
	       //*******************************************************************************//
	      // 							SQS  CODE										   //
	     //*******************************************************************************//
				
	}
	
    String tweet;
   
//    public Task(String s){
//        this.tweet = s;
//    }
  
    @Override
    public void run(){
    	//String tweet="";
       try{
    	
    	   //*******************************************************************************//
 	      // 							SQS  CODE										   //
 	     //*******************************************************************************//
    	   
    	// Receive messages
    	   //String queueUrl = "MyQueue";
    	   
    	    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest().withMaxNumberOfMessages(1)
				    .withQueueUrl(queueUrl);
			
			ReceiveMessageResult rx = bufferedSqs.receiveMessage(receiveMessageRequest);
			
			
			List<Message> messages = rx.getMessages();
			for (Message message : messages) {
				
				tweet=getMessage(message.getBody());
				for (Entry<String, String> entry : message.getAttributes().entrySet()) {
					System.out.println("  Attribute");
					System.out.println("    Name:  " + entry.getKey());
					System.out.println("    Value: " + entry.getValue());
				}
				
				AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile("api_key.txt");
	            //System.out.println("received "+tweet);
	            Document doc = alchemyObj.TextGetTextSentiment(tweet);
	            //System.out.println(ThreadPool.getStringFromDocument(doc));
	            
	            NodeList mNodeList = doc.getElementsByTagName("docSentiment");
	            for(int i=0;i<mNodeList.getLength();i++)
	            {
	            	Node mNode= mNodeList.item(i);
	            	if(mNode.getNodeType() == Node.ELEMENT_NODE)
	            	{
	            		Element mElement = (Element)mNode;
	            		System.out.println(tweet+" : "+mElement.getElementsByTagName("score").item(0).getTextContent()+" : "+mElement.getElementsByTagName("type").item(0).getTextContent());
	            		sqsclient.sendMessage(myQueue1, tweet+":"+mElement.getElementsByTagName("score").item(0).getTextContent()+":"+mElement.getElementsByTagName("type").item(0).getTextContent()+":"+getLatitude(message.getBody())+":"+getLongitude(message.getBody())+":"+getSkey(message.getBody())+":"+getCreatedAt(message.getBody()) );
	            	}
	            }
	            
			}
    	   
	    	   //*******************************************************************************//
	 	      // 							SQS  CODE										   //
	 	     //*******************************************************************************//
			
			
       }catch(Exception e){
    	   System.out.println("Some Exception:"+e);
       }
    }
    
    static String getMessage(String tweetmessage){
		String s[] = tweetmessage.split(",");
		String tweettext = s[0];
		return tweettext;
		//System.out.println("Tweet : "+tweettext+" --- Latitude --> "+latitude+" --- Longitude --> "+longitude);
	}
    
    static String getId(String tweetmessage){
    	String s[] = tweetmessage.split(",");
		String tweetid = s[1];
		return tweetid;
	}
    
    static String getLatitude(String tweetmessage){
    	String s[] = tweetmessage.split(",");
    	String latitude = s[2];
		return latitude;
	}
    
    static String getLongitude(String tweetmessage){
    	String s[] = tweetmessage.split(",");
		String longitude = s[3];
		return longitude;
	}
    
    static String getSkey(String tweetmessage){
    	String s[] = tweetmessage.split(",");
    	String skey = s[4];
		return skey;
	}
    
    static String getCreatedAt(String tweetmessage){
    	String s[] = tweetmessage.split(",");
    	String created = s[5];
		return created;
	}
  
    private static void sqsInit(AWSCredentials credentials) throws Exception {

    	int exist=0;
    	//credentials = new BasicAWSCredentials(YOUR_SECRET_KEY,YOUR_ACCESS_SECRET);
    	sqsclient = new AmazonSQSClient(credentials);
    	usWest2 = Region.getRegion(Regions.US_WEST_2);
    	sqsclient.setRegion(usWest2);
    	//System.out.println("Creating a new SQS queue called Response Queue.\n");
    	for (String queueUrl : sqsclient.listQueues().getQueueUrls()) {
    		//System.out.println("  QueueUrl: " + queueUrl);
    		if(queueUrl.equals("Responseq")){
    			exist = 1;
    		}
    	}
    	if(exist==1){
    		System.out.println("Queue Exist");
    	}
    	else{
    		CreateQueueRequest createQueueRequest = new CreateQueueRequest("Responseq");
    		myQueue1 = sqs.createQueue(createQueueRequest).getQueueUrl();
    		System.out.println("Queue "+myQueue1+" created");
    	}
    }
}




