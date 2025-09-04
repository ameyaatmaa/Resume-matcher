package cloud.resumematcher.services;

import cloud.resumematcher.helper.ResumeResult;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class ResumeService {

    private final JobMatchService jobMatchService;
    private final String storageConnectionString;
    private final String containerName;
    private final String formRecognizerKey;
    private final String formRecognizerEndpoint;

    public ResumeService(JobMatchService jobMatchService,
                         @Value("${AZURE_STORAGE_CONNECTION_STRING:${azure.storage.connection-string:}}") String storageConnectionString,
                         @Value("${AZURE_BLOB_CONTAINER:${azure.blob.container:resumes}}") String containerName,
                         @Value("${FORM_RECOGNIZER_KEY:${form.recognizer.key:}}") String formRecognizerKey,
                         @Value("${FORM_RECOGNIZER_ENDPOINT:${form.recognizer.endpoint:}}") String formRecognizerEndpoint) {
        this.jobMatchService = jobMatchService;
        this.storageConnectionString = storageConnectionString;
        this.containerName = containerName == null || containerName.isBlank() ? "resumes" : containerName;
        this.formRecognizerKey = formRecognizerKey;
        this.formRecognizerEndpoint = formRecognizerEndpoint;
    }

    public ResumeResult processResume(MultipartFile file, String userId) {
        try {
            String blobName = userId + "-" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
            BlobClient blobClient = new BlobClientBuilder()
                    .connectionString(storageConnectionString)
                    .containerName(containerName)
                    .blobName(blobName)
                    .buildClient();
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            DocumentAnalysisClient diClient = new DocumentAnalysisClientBuilder()
                    .credential(new AzureKeyCredential(formRecognizerKey))
                    .endpoint(formRecognizerEndpoint)
                    .buildClient();

            AnalyzeResult result = diClient.beginAnalyzeDocument(
                    "prebuilt-read",
                    BinaryData.fromStream(file.getInputStream(), file.getSize())
            ).getFinalResult();

            StringBuilder extractedText = new StringBuilder();
            for (DocumentPage page : result.getPages()) {
                page.getLines().forEach(l -> {
                    extractedText.append(l.getContent()).append(" ");
                });
            }

            Map<String, String> sections = parseSections(extractedText.toString());

            return jobMatchService.matchJobs(userId, sections);

        } catch (Exception e) {
            throw new RuntimeException("processing_failed: " + e.getMessage(), e);
        }
    }

    public ResumeResult getResults(String userId) {
        ResumeResult r = jobMatchService.getResult(userId);
        if (r == null) {
            Map<String, String> emptySections = new LinkedHashMap<>();
            emptySections.put("SUMMARY", "");
            emptySections.put("SKILLS", "");
            emptySections.put("PROJECTS", "");
            emptySections.put("EXPERIENCE", "");
            emptySections.put("EDUCATION", "");
            emptySections.put("CERTIFICATIONS", "");
            return new ResumeResult(userId, emptySections, 0, List.of());
        }
        return r;
    }

    public List<String> getJobSuggestions(String userId) {
        List<String> s = jobMatchService.getSuggestions(userId);
        return s == null ? new ArrayList<>() : s;
    }

    private Map<String, String> parseSections(String text) {
        Map<String, String> sections = new LinkedHashMap<>();
        String[] sectionNames = {"SUMMARY", "SKILLS", "PROJECTS", "EXPERIENCE", "EDUCATION", "CERTIFICATIONS"};

        for (int i = 0; i < sectionNames.length; i++) {
            String start = sectionNames[i];
            String end = (i + 1 < sectionNames.length) ? sectionNames[i + 1] : null;

            int startIdx = text.toUpperCase().indexOf(start);
            if (startIdx != -1) {
                int endIdx = (end != null) ? text.toUpperCase().indexOf(end, startIdx) : text.length();
                if (endIdx == -1) endIdx = text.length();
                String content = text.substring(startIdx + start.length(), endIdx).trim();
                sections.put(start, content);
            } else {
                sections.put(start, "");
            }
        }
        return sections;
    }
}
