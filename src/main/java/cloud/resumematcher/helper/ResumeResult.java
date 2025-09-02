package cloud.resumematcher.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeResult {
    private String userId;
    private String resumeText;
    private int matchScore;
    private List<String> suggestions;
}

