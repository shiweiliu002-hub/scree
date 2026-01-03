package org.example.screen.service;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.ByteArrayOutputStream;

@Service
public class ScreenshotService {

    public byte[] captureScreenshot(String url) throws Exception {
        WebDriver driver = null;
        try {
            // Configure Chrome options
            ChromeOptions options = new ChromeOptions();

            // Add headless and other necessary arguments
            options.addArguments("--headless=new"); // New headless mode
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-software-rasterizer");

            // Check if running in Docker environment (Linux with system Chrome)
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("linux")) {
                // Try multiple possible Chrome/Chromium paths
                String[] chromePaths = {
                    System.getenv().getOrDefault("CHROME_BIN", ""),
                    "/usr/bin/chromium-browser",  // Alpine
                    "/usr/bin/google-chrome",      // Debian/Ubuntu
                    "/usr/bin/chromium"            // Fallback
                };

                boolean foundChrome = false;
                for (String path : chromePaths) {
                    if (!path.isEmpty() && new java.io.File(path).exists()) {
                        options.setBinary(path);
                        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
                        foundChrome = true;
                        break;
                    }
                }

                // Fallback to WebDriverManager if no system Chrome found
                if (!foundChrome) {
                    WebDriverManager.chromedriver().setup();
                }
            } else {
                // Local development (Windows/Mac) - use WebDriverManager
                WebDriverManager.chromedriver().setup();
            }

            // Initialize WebDriver
            driver = new ChromeDriver(options);

            // Navigate to URL
            driver.get(url);

            // Wait for page to load
            Thread.sleep(2000);

            // Take screenshot
            TakesScreenshot screenshot = (TakesScreenshot) driver;
            return screenshot.getScreenshotAs(OutputType.BYTES);

        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
