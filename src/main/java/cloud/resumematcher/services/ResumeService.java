package cloud.resumematcher.services;

import cloud.resumematcher.helper.ResumeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.DetectEntitiesRequest;
import software.amazon.awssdk.services.comprehend.model.DetectEntitiesResponse;
import software.amazon.awssdk.services.comprehend.model.Entity;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.S3Object;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ResumeService {

    @Autowired
    private JobMatchService jobMatchService;

    @Value("${cloud.aws.region.static}")
    private String awsRegion;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private S3Client s3;
    private TextractClient textract;
    private ComprehendClient comprehend;
    private DynamoDbClient dynamoDb;

    @PostConstruct
    public void init() {
        Region region = Region.of(awsRegion);
        StaticCredentialsProvider creds = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );

        s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(creds)
                .build();

        textract = TextractClient.builder()
                .region(region)
                .credentialsProvider(creds)
                .build();

        comprehend = ComprehendClient.builder()
                .region(region)
                .credentialsProvider(creds)
                .build();

        dynamoDb = DynamoDbClient.builder()
                .region(region)
                .credentialsProvider(creds)
                .build();
    }

    public String processResume(MultipartFile file, String userId) {
        try {
            // 1. Upload to S3
            String key = "resumes/" + userId + "-" + file.getOriginalFilename();
            s3.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));

            // 2. Extract text with Textract
            DetectDocumentTextResponse textractResponse = textract.detectDocumentText(
                    DetectDocumentTextRequest.builder()
                            .document(Document.builder()
                                    .s3Object(S3Object.builder()
                                            .bucket(bucketName)
                                            .name(key)
                                            .build())
                                    .build())
                            .build());

            StringBuilder extractedText = new StringBuilder();
            for (Block block : textractResponse.blocks()) {
                if ("LINE".equals(block.blockTypeAsString())) {
                    extractedText.append(block.text()).append(" ");
                }
            }

            // 3. Analyze text with Comprehend
            DetectEntitiesResponse entities = comprehend.detectEntities(
                    DetectEntitiesRequest.builder()
                            .text(extractedText.toString())
                            .languageCode("en")
                            .build());

            List<Entity> entityList = entities.entities();

            // 4. Job Match Logic
            ResumeResult result = jobMatchService.matchJobs(userId, extractedText.toString(), entityList);

            // 5. Save to DynamoDB
            saveResultToDynamo(userId, result);

            return "Resume processed successfully for user: " + userId;

        } catch (Exception e) {
            throw new RuntimeException("Error processing resume: " + e.getMessage(), e);
        }
    }

    private void saveResultToDynamo(String userId, ResumeResult result) {
        dynamoDb.putItem(PutItemRequest.builder()
                .tableName("ResumeResults")
                .item(Map.of(
                        "userId", AttributeValue.builder().s(userId).build(),
                        "resumeText", AttributeValue.builder().s(result.getResumeText()).build(),
                        "matchScore", AttributeValue.builder().n(String.valueOf(result.getMatchScore())).build(),
                        "suggestions", AttributeValue.builder().s(String.join(",", result.getSuggestions())).build()
                ))
                .build());
    }

    public ResumeResult getResults(String userId) {
        return new ResumeResult(userId, "sample resume", 85, List.of("Software Engineer", "Backend Developer"));
    }

    public List<String> getJobSuggestions(String userId) {
        return List.of("Java Developer", "Cloud Engineer", "AI Engineer");
    }
}
