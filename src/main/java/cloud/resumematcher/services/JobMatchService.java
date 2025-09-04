package cloud.resumematcher.services;

import cloud.resumematcher.helper.ResumeResult;
import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobMatchService {

    private final TextAnalyticsClient taClient;
    private final Map<String, ResumeResult> store = new ConcurrentHashMap<>();

    public JobMatchService(@Value("${TEXT_ANALYTICS_ENDPOINT:${text.analytics.endpoint:}}") String taEndpoint,
                           @Value("${TEXT_ANALYTICS_KEY:${text.analytics.key:}}") String taKey) {
        if (taEndpoint != null && !taEndpoint.isBlank() && taKey != null && !taKey.isBlank()) {
            this.taClient = new TextAnalyticsClientBuilder()
                    .credential(new AzureKeyCredential(taKey))
                    .endpoint(taEndpoint)
                    .buildClient();
        } else {
            this.taClient = null;
        }
    }

    public ResumeResult matchJobs(String userId, String extractedText) {
        Map<String, List<String>> roleKeywords = new LinkedHashMap<>();
        roleKeywords.put("Java Developer", List.of("java","spring","spring boot","hibernate","jpa","microservices","rest","maven","gradle","kafka"));
        roleKeywords.put("Backend Engineer", List.of("api","rest","microservices","docker","kubernetes","postgresql","mysql","redis","rabbitmq","design patterns"));
        roleKeywords.put("Cloud Engineer", List.of("azure","blob","cosmos","functions","aks","devops","pipelines","terraform","arm","monitor"));
        roleKeywords.put("AI Engineer", List.of("machine learning","ml","pytorch","tensorflow","nlp","inference","model","classification","regression","vector"));
        roleKeywords.put("Data Engineer", List.of("spark","hadoop","etl","data pipeline","airflow","databricks","sql","snowflake","bigquery","kafka"));

        String text = extractedText == null ? "" : extractedText.toLowerCase(Locale.ROOT);
        Map<String,Integer> scores = new LinkedHashMap<>();
        for (Map.Entry<String,List<String>> e : roleKeywords.entrySet()) {
            int s = 0;
            for (String kw : e.getValue()) if (text.contains(kw)) s++;
            scores.put(e.getKey(), s);
        }

        List<Map.Entry<String,Integer>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a,b)->Integer.compare(b.getValue(), a.getValue()));
        List<String> suggestions = new ArrayList<>();
        for (int i=0;i<Math.min(5, sorted.size());i++) suggestions.add(sorted.get(i).getKey());

        int maxScore = sorted.isEmpty()?0:sorted.get(0).getValue();
        int normalized = Math.min(100, maxScore * 10);

        ResumeResult result = new ResumeResult(userId, extractedText, normalized, suggestions);
        store.put(userId, result);
        return result;
    }

    public ResumeResult getResult(String userId) {
        return store.get(userId);
    }

    public List<String> getSuggestions(String userId) {
        ResumeResult r = store.get(userId);
        if (r == null) return Collections.emptyList();
        return r.getSuggestions();
    }
}
