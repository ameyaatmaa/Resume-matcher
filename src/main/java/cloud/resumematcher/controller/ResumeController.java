package cloud.resumematcher.controller;

import cloud.resumematcher.helper.ResumeResult;
import cloud.resumematcher.services.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadResume(@RequestParam("file") MultipartFile file,
                                               @RequestParam("userId") String userId) {
        String result = resumeService.processResume(file, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/results/{userId}")
    public ResponseEntity<ResumeResult> getResults(@PathVariable String userId) {
        ResumeResult result = resumeService.getResults(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/jobsuggestions/{userId}")
    public ResponseEntity<List<String>> getJobSuggestions(@PathVariable String userId) {
        List<String> jobs = resumeService.getJobSuggestions(userId);
        return ResponseEntity.ok(jobs);
    }
}
