package cloud.resumematcher.services;

import cloud.resumematcher.helper.ResumeResult;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.comprehend.model.Entity;

import java.util.*;

@Service
public class JobMatchService {

    public ResumeResult matchJobs(String userId, String resumeText, List<Entity> entities) {
        int score = 0;
        List<String> suggestions = new ArrayList<>();

        if (resumeText.toLowerCase().contains("java")) {
            score += 30;
            suggestions.add("Java Developer");
        }
        if (resumeText.toLowerCase().contains("aws")) {
            score += 30;
            suggestions.add("Cloud Engineer");
        }
        if (resumeText.toLowerCase().contains("machine learning")) {
            score += 40;
            suggestions.add("AI Engineer");
        }

        return new ResumeResult(userId, resumeText, score, suggestions);
    }
}
