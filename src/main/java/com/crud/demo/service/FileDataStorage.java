package com.crud.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileDataStorage {

    private static final FileDataStorage INSTANCE = new FileDataStorage();
    private final Map<String, List<String>> fileDataMap = new HashMap<>();

    private FileDataStorage() {}

    public static FileDataStorage getInstance() {
        return INSTANCE;
    }

    // Store file data by file name
    public void setFileData(String fileName, List<String> fileData) {
        fileDataMap.put(fileName, fileData);
    }

    // Retrieve all file information
    public List<String> getAllFileInfo() {      
        return fileDataMap.values().stream()
                          .flatMap(List::stream)
                          .collect(Collectors.toList()); 
    }

    // Retrieve data for a specific file
    public List<String> getFileData(String fileName) {
        return fileDataMap.getOrDefault(fileName, new ArrayList<>()); 
    }
}
