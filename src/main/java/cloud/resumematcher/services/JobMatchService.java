package cloud.resumematcher.service;

import cloud.resumematcher.model.ResumeResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobMatchService {

    public ResumeResult matchJobs(String userId, String resumeText) {
        int score = 0;
        List<String> suggestions = new ArrayList<>();

        if (resumeText.toLowerCase().contains("java")) {
            score += 30;
            suggestions.add("Java Developer");
        }
        if (resumeText.toLowerCase().contains("azure")) {
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
