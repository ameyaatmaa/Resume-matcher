package cloud.resumematcher.service;

import cloud.resumematcher.model.ResumeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.storage.blob.BlobClientBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeService {

    @Autowired
    private JobMatchService jobMatchService;

    private final String storageConnectionString = "<YOUR_BLOB_CONNECTION_STRING>";
    private final String containerName = "resumes";

    private final String formRecognizerKey = "<FORM_RECOGNIZER_KEY>";
    private final String formRecognizerEndpoint = "<FORM_RECOGNIZER_ENDPOINT>";

    public String processResume(MultipartFile file, String userId) {
        try {
            // 1. Upload to Azure Blob
            String blobName = userId + "-" + file.getOriginalFilename();
            new BlobClientBuilder()
                    .connectionString(storageConnectionString)
                    .containerName(containerName)
                    .blobName(blobName)
                    .buildClient()
                    .upload(file.getInputStream(), file.getSize(), true);

            // 2. Extract text with Form Recognizer
            var client = new DocumentAnalysisClientBuilder()
                    .credential(new AzureKeyCredential(formRecognizerKey))
                    .endpoint(formRecognizerEndpoint)
                    .buildClient();

            AnalyzeResult result = client.beginAnalyzeDocumentFromUrl("prebuilt-read",
                    "https://<your-storage-account>.blob.core.windows.net/" + containerName + "/" + blobName,
                    new AnalyzeDocumentOptions()).getFinalResult();

            StringBuilder extractedText = new StringBuilder();
            for (DocumentPage page : result.getPages()) {
                page.getLines().forEach(line -> extractedText.append(line.getContent()).append(" "));
            }

            // 3. Match jobs
            ResumeResult resumeResult = jobMatchService.matchJobs(userId, extractedText.toString());

            // (Optional) Save to Cosmos DB here

            return "Resume processed for " + userId;

        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }
    }

    public ResumeResult getResults(String userId) {
        return new ResumeResult(userId, "sample text", 85, List.of("Java Developer", "Cloud Engineer"));
    }

    public List<String> getJobSuggestions(String userId) {
        return List.of("Backend Developer", "AI Engineer");
    }
}
