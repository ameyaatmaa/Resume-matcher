package cloud.resumematcher.helper;

import java.util.List;

public class ResumeResult {
    private String userId;
    private String extractedText;
    private int matchScore;
    private List<String> suggestions;

    public ResumeResult() {
    }

    public ResumeResult(String userId, String extractedText, int matchScore, List<String> suggestions) {
        this.userId = userId;
        this.extractedText = extractedText;
        this.matchScore = matchScore;
        this.suggestions = suggestions;
    }

    public String getUserId() {
        return userId;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
