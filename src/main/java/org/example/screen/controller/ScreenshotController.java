package org.example.screen.controller;

import org.example.screen.service.ScreenshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ScreenshotController {

    @Autowired
    private ScreenshotService screenshotService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/api/screenshot")
    @ResponseBody
    public ResponseEntity<?> captureScreenshot(@RequestBody Map<String, String> request) {
        try {
            String url = request.get("url");

            if (url == null || url.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "URL不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate URL format
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            // Capture screenshot
            byte[] screenshot = screenshotService.captureScreenshot(url);

            // Return base64 encoded image
            String base64Image = java.util.Base64.getEncoder().encodeToString(screenshot);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("image", "data:image/png;base64," + base64Image);
            response.put("url", url);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "截图失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/api/screenshot/download")
    public ResponseEntity<byte[]> downloadScreenshot(@RequestBody Map<String, String> request) {
        try {
            String url = request.get("url");

            if (url == null || url.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Validate URL format
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            // Capture screenshot
            byte[] screenshot = screenshotService.captureScreenshot(url);

            // Generate filename from URL
            String filename = url.replaceAll("[^a-zA-Z0-9]", "_") + ".png";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(screenshot);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
