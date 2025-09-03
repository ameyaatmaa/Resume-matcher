package cloud.resumematcher.controller;

import cloud.resumematcher.model.ResumeResult;
import cloud.resumematcher.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/upload")
    public String uploadResume(@RequestParam("file") MultipartFile file,
                               @RequestParam("userId") String userId) {
        return resumeService.processResume(file, userId);
    }

    @GetMapping("/results/{userId}")
    public ResumeResult getResults(@PathVariable String userId) {
        return resumeService.getResults(userId);
    }

    @GetMapping("/suggestions/{userId}")
    public List<String> getSuggestions(@PathVariable String userId) {
        return resumeService.getJobSuggestions(userId);
    }
}
