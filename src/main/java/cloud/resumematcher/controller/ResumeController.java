package cloud.resumematcher.controller;

import cloud.resumematcher.helper.ResumeResult;
import cloud.resumematcher.services.ResumeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeResult upload(@RequestParam("file") MultipartFile file, @RequestParam("userId") String userId) {
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
