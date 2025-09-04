package cloud.resumematcher.helper;

import java.util.List;
import java.util.Map;

public class ResumeResult {
    private String userId;
    private Map<String, String> sections; // Key: section name, Value: content
    private int matchScore;
    private List<String> suggestions;

    public ResumeResult() {}

    public ResumeResult(String userId, Map<String, String> sections, int matchScore, List<String> suggestions) {
        this.userId = userId;
        this.sections = sections;
        this.matchScore = matchScore;
        this.suggestions = suggestions;
    }

    public String getUserId() { return userId; }
    public Map<String, String> getSections() { return sections; }
    public int getMatchScore() { return matchScore; }
    public List<String> getSuggestions() { return suggestions; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setSections(Map<String, String> sections) { this.sections = sections; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
}
