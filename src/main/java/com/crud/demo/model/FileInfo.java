package com.crud.demo.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private LocalDateTime uploadDate;
    private String uploadedBy;

    public FileInfo() {}

    public FileInfo(String fileName, LocalDateTime uploadDate, String uploadedBy) {
        this.fileName = fileName;
        this.uploadDate = uploadDate;
        this.uploadedBy = uploadedBy;
    }

    // Getters and Setters
}
