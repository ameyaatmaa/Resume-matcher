package cloud.resumematcher.services;

import cloud.resumematcher.helper.ResumeResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobMatchService {

    private final Map<String, ResumeResult> store = new ConcurrentHashMap<>();

    public ResumeResult matchJobs(String userId, Map<String, String> sections) {
        Map<String, Map<String, Integer>> roleKeywords = new LinkedHashMap<>();
        roleKeywords.put("Java Developer", Map.of("java",3,"spring",3,"spring boot",3,"hibernate",2,"rest",2,"maven",2,"gradle",2,"kafka",1));
        roleKeywords.put("Backend Engineer", Map.of("api",2,"rest",2,"microservices",2,"docker",2,"kubernetes",2,"postgresql",2,"mysql",2,"redis",1,"rabbitmq",1,"design patterns",2));
        roleKeywords.put("Cloud Engineer", Map.of("azure",3,"blob",2,"cosmos",2,"functions",2,"aks",2,"devops",2,"pipelines",2,"terraform",2,"arm",1,"monitor",1));
        roleKeywords.put("AI Engineer", Map.of("machine learning",3,"ml",2,"pytorch",2,"tensorflow",2,"nlp",2,"inference",1,"model",1,"classification",1,"regression",1,"vector",1));
        roleKeywords.put("Data Engineer", Map.of("spark",3,"hadoop",3,"etl",2,"data pipeline",2,"airflow",2,"databricks",2,"sql",2,"snowflake",2,"bigquery",2,"kafka",1));

        Map<String, Integer> scores = new LinkedHashMap<>();
        for (String role : roleKeywords.keySet()) scores.put(role, 0);

        for (Map.Entry<String, String> sectionEntry : sections.entrySet()) {
            String section = sectionEntry.getKey();
            String text = sectionEntry.getValue().toLowerCase();

            int weight = switch (section) {
                case "TECHNICAL SKILLS" -> 3;
                case "PROJECTS", "EXPERIENCE" -> 2;
                default -> 1;
            };

            for (String role : roleKeywords.keySet()) {
                Map<String,Integer> kws = roleKeywords.get(role);
                for (Map.Entry<String,Integer> kw : kws.entrySet()) {
                    if (text.contains(kw.getKey())) {
                        scores.put(role, scores.get(role) + kw.getValue() * weight);
                    }
                }
            }
        }

        List<Map.Entry<String,Integer>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a,b) -> Integer.compare(b.getValue(), a.getValue()));

        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < Math.min(5, sorted.size()); i++) {
            if (sorted.get(i).getValue() > 0) suggestions.add(sorted.get(i).getKey());
        }

        int maxScore = sorted.isEmpty() ? 0 : sorted.get(0).getValue();
        int normalized = Math.min(100, maxScore * 10);

        ResumeResult result = new ResumeResult();
        result.setUserId(userId);
        result.setSections(sections);
        result.setMatchScore(normalized);
        result.setSuggestions(suggestions);

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
