package com.nyu.tweettrend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.Tables;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class UpdateTweets implements Runnable {

//	static BasicAWSCredentials credentials = new BasicAWSCredentials(YOUR_SECRET_KEY,ACCESS_SECRET_KEY);
	private static final String KEY_UUID="uuid";
	private static final String KEY_KEYWORD="searchkey";
	private static final String KEY_LATITUDE="lat";
	private static final String KEY_LONGITUDE="lng";
	final static String tableName = "tweetslocation";
	private static AmazonDynamoDBClient dynamoDB;
	private static DynamoDB db;
	private static ConfigurationBuilder cb;
	private static AmazonSQS sqs;
	static String myQueue;
	
	public static int count=0;
	
	static String skey="",statusT="";
	
	private TwitterStream twitterStream;
	 static ArrayList<TweetsData> tweetList;
	 	
	 
	@Override
	public void run() {
		System.out.println("Background process started");	
		final String[] keywordsArray = { "sport","music", "game","mobile","food", "restaurant ","#halloween","us","#thanks","worldcup" ,"job"};
	    FilterQuery filtr = new FilterQuery(keywordsArray);
	    filtr.track(keywordsArray);
	    
	    
		twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

		StatusListener listener = new StatusListener() {

			private int totalCount=0;

			@Override
			public void onException(Exception arg0) {
				System.out.println("onEXception" + arg0.getMessage()+"*******************");

			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
				//System.out.println("onTrackLimitation");
			}

			@Override
			public void onStatus(Status status) {
			
				try {
		
					if(status.getGeoLocation()!=null)
					{
						++totalCount;
						System.out.println("Total Tweets Received : "+totalCount);
						for(String keyword : keywordsArray)
						{
							if(status.getText().contains(keyword))
							{
								System.out.println("tweet : "+status.getText());
								tweetList.add(new TweetsData(UUID.randomUUID().toString(), keyword, status.getGeoLocation().getLatitude(), status.getGeoLocation().getLongitude(),String.valueOf(status.getId()),status.getCreatedAt(),status.getText()));
								sqs.sendMessage(myQueue, status.getText()+","+status.getId()+","+status.getGeoLocation().getLatitude()+","+status.getGeoLocation().getLongitude()+","+keyword+","+status.getCreatedAt());
								break;
							}
							
						}
						
						if(totalCount==10)
						{
							DataEntryDynamo(tweetList);
							totalCount=0;
						}
					}
				}
						
				 catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			
			}

			@Override
			public void onStallWarning(StallWarning stat) {
				System.out.println("onStallWarning");
			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {
				System.out.println("onScrubGeo");

			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
				// TODO Auto-generated method stub

			}
		};

		twitterStream.addListener(listener);
		
		 twitterStream.firehose(50);
		 twitterStream.filter(filtr);


	}

	protected void DataEntryDynamo(ArrayList<TweetsData> tweetsCollection) 
	{
		PutItemRequest putItemRequest;
		PutItemResult putItemResult;
		System.out.println("Size of Tweets Data : "+tweetsCollection.size());
		for(TweetsData data : tweetsCollection)
		{
			Map<String, AttributeValue> item = newItem(data);
			putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);
		}
		
		tweetList.clear();
	}

	private static Map<String, AttributeValue> newItem(TweetsData data) {
		
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		
		item.put("uuid", new AttributeValue(data.uuid));
		item.put("searchkey", new AttributeValue(data.searchkey));
		item.put("lat", new AttributeValue(data.lat.toString()));
		item.put("lng", new AttributeValue(data.lng.toString()));
		item.put("tweetid",new AttributeValue(data.tweetid));
		item.put("tweettime",new AttributeValue(data.tweettime.toString()));
		item.put("tweetText",new AttributeValue(data.tweetText));
		
		return item;
	}
	

	class TweetsData
	{
		String uuid;
		String searchkey;
		Double lat;
		Double lng;
		String tweetid;
		Date tweettime;
		String tweetText;
		
		public TweetsData(String uuid, String searchkey, Double lat,Double lng,String tweetid,Date tweettime, String tweetText) 
		{
			this.uuid = uuid;
			this.searchkey = searchkey;
			this.lat = lat;
			this.lng = lng;
			this.tweetid=tweetid;
			this.tweettime = tweettime;
			this.tweetText = tweetText;
		}
		
	}

	public static void init() throws Exception{

		sqsInit();
		twitterInit();
		dynamoInit();
		tweetList= new ArrayList<TweetsData>();
		
	}
	
	private static void sqsInit() throws Exception {
		
		int exist=0;
		//BasicAWSCredentials credentials = new BasicAWSCredentials(ReadPropertiesFile.getInstance("AwsCredentials.properties").getKey("secretKey"),ReadPropertiesFile.getInstance("AwsCredentials.properties").getKey("accessKey"));
		AWSCredentials credentials = new ProfileCredentialsProvider("default").getCredentials();
		sqs = new AmazonSQSClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        sqs.setRegion(usWest2);
        System.out.println("Creating a new SQS queue called MyQueue.\n");
        for (String queueUrl : sqs.listQueues().getQueueUrls()) {
            System.out.println("  QueueUrl: " + queueUrl);
            if(queueUrl.equals("MyQueue")){
            	exist = 1;
            }
        }
        if(exist==1){
        	System.out.println("Queue Exist");
        }
        else{
		        CreateQueueRequest createQueueRequest = new CreateQueueRequest("MyQueue");
		        myQueue = sqs.createQueue(createQueueRequest).getQueueUrl();
		        System.out.println("Queue "+myQueue+" created");
        }
	}

	private static void dynamoInit() throws Exception {
		//BasicAWSCredentials credentials = new BasicAWSCredentials(ReadPropertiesFile.getInstance("AwsCredentials.properties").getKey("secretKey"),ReadPropertiesFile.getInstance("AwsCredentials.properties").getKey("accessKey"));
		AWSCredentials credentials = new ProfileCredentialsProvider("default").getCredentials();
//		try {
//			credentials = new ProfileCredentialsProvider("default").getCredentials();
//		} catch (Exception e) {
//			throw new AmazonClientException(
//					"Cannot load the credentials from the credential profiles file. " +
//							"Please make sure that your credentials file is at the correct " +
//							"location (C:\\Users\\Swar\\.aws\\credentials), and is in valid format.",e);
//		}
		
		dynamoDB = new AmazonDynamoDBClient(credentials);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		dynamoDB.setRegion(usWest2);
		db = new DynamoDB(dynamoDB);
	
		try {
			
			// Create table if it does not exist yet
			
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
			
				// Create a table with a primary hash key named 'name', which holds a string
			
				CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
						.withKeySchema(new KeySchemaElement().withAttributeName("uuid").withKeyType(KeyType.HASH))
						.withAttributeDefinitions(new AttributeDefinition().withAttributeName("uuid").withAttributeType(ScalarAttributeType.S))
						.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
				
				TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
				System.out.println("Created Table: " + createdTableDescription);

				// Wait for it to become active
				
				System.out.println("Waiting for " + tableName + " to become ACTIVE...");
				Tables.awaitTableToBecomeActive(dynamoDB, tableName);
			}

			// Describe our new table
			
			DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
			TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
			System.out.println("Table Description: " + tableDescription);
		} catch (AmazonServiceException ase) {
			
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}

	}

	private static void twitterInit() throws IOException {
		
		cb = new ConfigurationBuilder();
		
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(ReadPropertiesFile.getInstance("TweetCredentials.properties").getKey("consumerKey"))
		.setOAuthConsumerSecret(ReadPropertiesFile.getInstance("TweetCredentials.properties").getKey("consumerSecret"))
		.setOAuthAccessToken(ReadPropertiesFile.getInstance("TweetCredentials.properties").getKey("accessToken"))
		.setOAuthAccessTokenSecret(ReadPropertiesFile.getInstance("TweetCredentials.properties").getKey("accessTokenSecret"));
		
		/*cb.setDebugEnabled(true)
		.setOAuthConsumerKey("oaDxyWPbtMR4onc5DrLXe63xA")
		.setOAuthConsumerSecret("ANtXjlaKko0H5rzhunUpa5lPJZZ1u63ziXmc50hsMv4tBdqIJ1")
		.setOAuthAccessToken("2771061159-ovHIYssc2kMJ4ntTCi1MtcuciaNG3MgLi2RBGyI")
		.setOAuthAccessTokenSecret("dsyjMTOH89M4QYsUJlfHnoUaWeFfYeLnmJWRFquMwSCZ2");*/
	}

	
}