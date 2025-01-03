package com.crud.demo.controller;

import com.crud.demo.service.ChatbotService;
import com.crud.demo.service.FileDataService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatbot")
@CrossOrigin(origins = "http://localhost:8080")
public class ChatbotController {

    @Autowired
    private final ChatbotService chatbotService;
    private final FileDataService fileDataService;
	private char[] stackTraceContent;

    // Constructor Injection
    public ChatbotController(ChatbotService chatbotService, FileDataService fileDataService) {
        this.chatbotService = chatbotService;
        this.fileDataService = fileDataService;
    }

    // Handle user query
    @PostMapping("/query")
    public ResponseEntity<Map<String, String>> handleQuery(@RequestBody Map<String, String> userQuery, HttpSession session) {
        String query = userQuery.get("query");
        List<String> fileData = (List<String>) session.getAttribute("uploadedFileData");

        // Check if file data is uploaded
        if (fileData == null || fileData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("response", "No file uploaded. Please upload a file first."));
        }

        String response = chatbotService.processQuery(query, fileData);
        return ResponseEntity.ok(Collections.singletonMap("response", response));
    }

    // Handle file upload
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, HttpSession session) {
        try {
            List<String> fileData = fileDataService.saveAndParseFile(file);
            session.setAttribute("uploadedFileData", fileData); // Store file data in session
            return ResponseEntity.ok("File uploaded and processed successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed.");
        }
    }




    // Helper method to extract the stack trace for a specific exception name
    private String getStackTraceForException(List<String> uploadedFileData, String exceptionName) {
        StringBuilder stackTrace = new StringBuilder();
        // Search through uploadedFileData for the stack trace related to the exception name
        for (String line : uploadedFileData) {
            if (line.contains(exceptionName)) {
                stackTrace.append(line).append("\n");
            }
        }
        return stackTrace.toString(); // Return all matching stack trace lines
    }


}
