package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.metricstoragesystem;





import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.SystemUtil;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This sample demonstrates how to perform a few simple operations with the
 * Amazon DynamoDB service, can be found here
 * https://github.com/aws/aws-sdk-java/blob/master/src/samples/AmazonDynamoDB/AmazonDynamoDBSample.java.
 */

public class AWSDynamoDatabase {
    private static final String TABLE_NAME = "MazeRunnerMetrics";
    private static final String PARTITION_KEY = "mazeFilename";
    private static final String SHORT_KEY = "hashInputs";
    public static final String X_START = "xStart";
    public static final String Y_START = "yStart";
    public static final String X_FINAL = "xFinal";
    public static final String Y_FINAL = "yFinal";
    public static final String VELOCITY = "velocity";
    public static final String STRATEGY = "strategy";
    public static final String BRANCH_COUNT = "branchCount";

        /*
     * Before running the code:
     *      Fill in your AWS access credentials in the provided credentials
     *      file template, and be sure to move the file to the default location
     *      (~/.aws/credentials) where the sample code will load the
     *      credentials from.
     *      https://console.aws.amazon.com/iam/home?#security_credential
     *
     * WARNING:
     *      To avoid accidental leakage of your credentials, DO NOT keep
     *      the credentials file in your source directory.
     */

    static AmazonDynamoDB dynamoDB;

    /*
    +--------------------+--------------+-----+----+----+----+----------+----------+------------+
    |    SORT_KEY        |  PARTION_KEY |  x0 | y0 | x1 | y1 | velocity | strategy | basicBlocs |
    +--------------------+--------------+-----+----+----+----+----------+----------+------------+
    | aslfgjlasdjkfkasf  | maze100.maze |  2  |  3 | 5  | 19 |   20     |  astar   |  1454984   |
    | mkhen5alnfwefwe5   | maze50.maze  |  5  |  10| 19 | 30 |   50     |  astar   |  834753423 |
    +--------------------+--------------+-----+----+----+----+----------+----------+------------+
    view https://aws.amazon.com/blogs/database/choosing-the-right-dynamodb-partition-key/

    */
    private static void init() throws Exception {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(credentialsProvider)

                .withRegion(SystemUtil.AVAILABILITY_ZONE)
                .build();
    }


    public static void main(String[] args) {
        try {
            init();
            createTable();
            addRow("maze", 01, 0, 10, 10, 0, "astar",10);
            ScanResult request = getTuple("maze", 01,  0, 10, 10);
            for (Map<String, AttributeValue> result: request.getItems()) {
                for(Map.Entry<String, AttributeValue> keypair : result.entrySet()){
                    System.out.println(keypair.getKey() + " -> "
                            + (keypair.getValue().getS() == null ? keypair.getValue().getN()
                                                                 : keypair.getValue().getS()));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public AWSDynamoDatabase(){
        try {
            init();
            createTable();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @return Number of Rows in Table
     */
    public static int getNumberOfEntries() {
        ScanRequest scanRequest = new ScanRequest(TABLE_NAME);
        ScanResult res = dynamoDB.scan(scanRequest);
        return res.getCount();
    }

    public static PutItemResult addRow(String mazeFileName, int xStart, int yStart, int xFinal, int yFinal, int velocity, String strategy, long countBranches){
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();

        String s = makeHash(mazeFileName, xStart, yStart, xFinal, yFinal, velocity, strategy, countBranches);

        item.put(PARTITION_KEY, new AttributeValue(mazeFileName));
        item.put(SHORT_KEY, new AttributeValue(s));
        item.put(X_START, new AttributeValue().withN(Integer.toString(xStart)));
        item.put(Y_START, new AttributeValue().withN(Integer.toString(yStart)));
        item.put(X_FINAL, new AttributeValue().withN(Integer.toString(xFinal)));
        item.put(Y_FINAL, new AttributeValue().withN(Integer.toString(yFinal)));
        item.put(VELOCITY, new AttributeValue().withN(Integer.toString(velocity)));
        item.put(STRATEGY, new AttributeValue(strategy));
        item.put(BRANCH_COUNT, new AttributeValue().withS(Long.toString(countBranches)));
        System.out.println("Wrote to Dynamo");
        PutItemRequest putItemRequest = new PutItemRequest(TABLE_NAME, item);
        return dynamoDB.putItem(putItemRequest);

    }



    private static String makeHash(String mazeFileName, int xStart, int yStart, int xFinal, int yFinal, int velocity, String strategy, long countBasicBlocks) {
        String preHash = mazeFileName + xStart + yStart + xFinal + yFinal + velocity + strategy + countBasicBlocks;
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(preHash.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printBase64Binary(hash);
    }

    public static void createTable(){
        // Create a table with a primary hash key named 'name', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(TABLE_NAME)
                .withKeySchema(new KeySchemaElement().withAttributeName(PARTITION_KEY).withKeyType(KeyType.HASH),
                        new KeySchemaElement().withAttributeName(SHORT_KEY).withKeyType(KeyType.RANGE))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName(PARTITION_KEY).withAttributeType(ScalarAttributeType.S),
                                          new AttributeDefinition().withAttributeName(SHORT_KEY).withAttributeType(ScalarAttributeType.S    ))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
        // wait for the table to move into ACTIVE state
        try {
            TableUtils.waitUntilActive(dynamoDB, TABLE_NAME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Describe our new table
        DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(TABLE_NAME);
        TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
        System.out.println("Table Description: " + tableDescription);
    }

    public static ScanResult getTuple(String mazeFileName, int xStart, int yStart, int xFinal, int yFinal) {
        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
        Condition condition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withN(Float.toString(xStart)));
        scanFilter.put(X_START, condition);
        Condition condition2 = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withN(Float.toString(yStart)));
        scanFilter.put(Y_START, condition2);
        Condition condition3 = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withN(Float.toString(xFinal)));
        scanFilter.put(X_FINAL, condition3);
        Condition condition4 = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withN(Float.toString(yFinal)));
        scanFilter.put(Y_FINAL, condition4);

        ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        List<Map<String,AttributeValue>> info = scanResult.getItems();

            return dynamoDB.scan(scanRequest);
    }

    public static ScanResult getForMaze(String mazeFileName) {
        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
        Condition condition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(mazeFileName));

        ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        List<Map<String,AttributeValue>> info = scanResult.getItems();

        return dynamoDB.scan(scanRequest);
    }







}
