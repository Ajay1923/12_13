package com.crud.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FileDataService {

    private final List<String> uploadedFileInfo = new ArrayList<>();

    // Save the file and parse its content
    public List<String> saveAndParseFile(MultipartFile file) throws IOException {
        List<String> fileContent = parseFile(file);
        uploadedFileInfo.clear();
        uploadedFileInfo.addAll(fileContent);
        return new ArrayList<>(uploadedFileInfo);
    }

    // Retrieve all uploaded file content
    public List<String> getAllUploadedFileInfo() {
        return new ArrayList<>(uploadedFileInfo);
    }

    // Parse the file content line by line
    private List<String> parseFile(MultipartFile file) throws IOException {
        List<String> content = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.add(line);
            }
        }
        return content;
    }

	public List<String> getUploadedFileData() {
		// TODO Auto-generated method stub
		return null;
	}
	public Map<String, Long> getExceptionCounts() {
        if (uploadedFileInfo.isEmpty()) {
            return Collections.emptyMap();
        }

        return uploadedFileInfo.stream()
                .filter(line -> line.contains("Exception")) // Filter lines containing exceptions
                .map(this::extractExceptionName)            // Extract exception names
                .collect(Collectors.groupingBy(name -> name, Collectors.counting())); // Count occurrences
    }

    // New helper method: Extract exception name from a log line
    private String extractExceptionName(String logLine) {
        int colonIndex = logLine.indexOf(":");
        if (colonIndex > -1) {
            return logLine.substring(0, colonIndex).trim();
        }
        return "Unknown Exception";
    }
}
