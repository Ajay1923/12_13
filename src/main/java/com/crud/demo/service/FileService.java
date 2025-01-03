package com.crud.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final Path uploadDirectory = Paths.get("uploads");

    public FileService() {
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String saveFile(MultipartFile file) throws IOException {
        Path filePath = uploadDirectory.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath);
        return "File uploaded successfully: " + file.getOriginalFilename();
    }

    public List<String> getAllUploadedFilesInfo() throws IOException {
        return Files.list(uploadDirectory)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
    }
}
