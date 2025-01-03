package com.crud.demo.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatbotService {

    private final FileDataService fileDataService;

    public ChatbotService(FileDataService fileDataService) {
        this.fileDataService = fileDataService;
    }

    public String processQuery(String query, List<String> uploadedFileData) {
        System.out.println("Processing query: " + query); // Log user query
        System.out.println("Uploaded file data: " + uploadedFileData); // Log uploaded file data

        query = query.toLowerCase();
        if (query.contains("uploaded files")) {
            return uploadedFileData.isEmpty() ? "No files have been uploaded yet." : "Uploaded files:\n" + String.join("\n", uploadedFileData);
        }

        if (query.contains("exception count")) {
            StringBuilder response = new StringBuilder();
            response.append("Log counts:\n");

            Map<String, Integer> counts = countLogOccurrences(uploadedFileData);

            List<String> specificExceptions = Arrays.asList(
                "NullPointerException", 
                "ValidationException", 
                "SchedulerException", 
                "AccessException", 
                "InvalidFormatException", 
                "CloudClientException", 
                "SuperCsvException"
            );

            for (String exception : specificExceptions) {
                if (query.contains(exception.toLowerCase() + " count")) {
                	response.append(exception).append(": ").append(counts.getOrDefault(exception, 0)).append("\n");
                    return getExceptionCountResponse(uploadedFileData, Collections.singletonList(exception));
                }
            }
            response.append("AccessException: ").append(counts.getOrDefault("AccessException", 0)).append("\n");
            response.append("ValidationException: ").append(counts.getOrDefault("ValidationException", 0)).append("\n");
            response.append("SchedulerException: ").append(counts.getOrDefault("SchedulerException", 0)).append("\n");
            response.append("NullPointerException: ").append(counts.getOrDefault("NullPointerException", 0)).append("\n");
            response.append("InvalidFormatException: ").append(counts.getOrDefault("InvalidFormatException", 0)).append("\n");
            response.append("CloudClientException: ").append(counts.getOrDefault("CloudClientException", 0)).append("\n");
            response.append("SuperCsvException: ").append(counts.getOrDefault("SuperCsvException", 0)).append("\n");
            response.append("ERROR: ").append(counts.getOrDefault("ERROR", 0)).append("\n");
            response.append("INFO: ").append(counts.getOrDefault("INFO", 0)).append("\n");
            response.append("DEBUG: ").append(counts.getOrDefault("DEBUG", 0)).append("\n");

            return response.toString();
        }
        if (query.contains("error count")) {
            return String.format("ERROR count: %d", countLogLevel(uploadedFileData, "ERROR"));
        }
        

        if (query.contains("info count")) {
            return String.format("INFO count: %d", countLogLevel(uploadedFileData, "INFO"));
        }

        if (query.contains("debug count")) {
            return String.format("DEBUG count: %d", countLogLevel(uploadedFileData, "DEBUG"));
        }

        if (query.contains("stacktrace")) {
            return handleStackTraceQuery(query, uploadedFileData);
        }

        if (query.contains("hello") || query.contains("hi")) {
            return "Hello! How can I assist you today?";
        }

        if (query.contains("help")) {
			return  "Greet me with 'Hello' or 'Hi'\n" +
					" I can help you with the following:\n"+
                   "Ask for exception counts \n"  +
                   "Ask for ERROR, INFO, or DEBUG counts  \n" +
                   "Ask for a particular exception's stacktrace (e.g., 'NullPointerException stacktrace') \n" +
                   "Just type your question!";
        }
        

        // Searching for the query in the uploaded file data
        StringBuilder response = new StringBuilder();
        int count = 0; 
        for (String line : uploadedFileData) {
            if (line.toLowerCase().contains(query)) {
                response.append("I found this in the uploaded files: ").append(line).append("\n");
                count++;
            }
            if (count >= 1000) break;  
        }
        if (response.length() > 0) {
            return response.toString();
        }
        return "I'm sorry, the query is not vaild.";
    }

    private String getExceptionCountResponse(List<String> uploadedFileData, List<String> exceptions) {
        StringBuilder response = new StringBuilder();
        Map<String, Integer> counts = countLogOccurrences(uploadedFileData);
        
        for (String exception : exceptions) {
            response.append(exception).append(": ").append(counts.getOrDefault(exception, 0)).append("\n");
        }
        // Include counts for ERROR, INFO, DEBUG logs as well
        if (exceptions.isEmpty()) {
            response.append("ERROR: ").append(counts.getOrDefault("ERROR", 0)).append("\n");
            response.append("INFO: ").append(counts.getOrDefault("INFO", 0)).append("\n");
            response.append("DEBUG: ").append(counts.getOrDefault("DEBUG", 0)).append("\n");
            response.append(exceptions).append(": ").append(counts.getOrDefault(exceptions, 0)).append("\n");
        }

        return response.toString();
    }

	private Map<String, Integer> countLogOccurrences(List<String> uploadedFileData) {
        Map<String, Integer> logCounts = new HashMap<>();

        List<String> specificExceptions = Arrays.asList(
            "NullPointerException", 
            "ValidationException", 
            "SchedulerException", 
            "AccessException", 
            "InvalidFormatException", 
            "CloudClientException", 
            "SuperCsvException"
        );

        for (String exception : specificExceptions) {
            logCounts.put(exception, 0);
        }

        logCounts.put("ERROR", 0);
        logCounts.put("INFO", 0);
        logCounts.put("DEBUG", 0);

        for (String line : uploadedFileData) {
            for (String exception : specificExceptions) {
                if (line.contains(exception)) {
                    logCounts.put(exception, logCounts.get(exception) + 1);
                }
            }

            if (line.contains("ERROR")) {
                logCounts.put("ERROR", logCounts.get("ERROR") + 1);
            } else if (line.contains("INFO")) {
                logCounts.put("INFO", logCounts.get("INFO") + 1);
            } else if (line.contains("DEBUG")) {
                logCounts.put("DEBUG", logCounts.get("DEBUG") + 1);
            } 
        }

        return logCounts;
    }

    private String handleStackTraceQuery(String query, List<String> uploadedFileData) {
        String exceptionName = query.replace("stacktrace", "").trim();
        if (!exceptionName.isEmpty()) {
            String stackTrace = getStackTraceForException(uploadedFileData, exceptionName);
            return stackTrace.isEmpty() ? "No stack trace found for " + exceptionName : stackTrace;
        } else {
            return "Please specify the exception name for the stack trace (e.g., 'NullPointerException stacktrace').";
        }
    }

    private String getStackTraceForException(List<String> fileData, String exceptionName) {
        List<String> detailedLogs = extractDetailedErrorLogs(fileData);
        StringBuilder stackTrace = new StringBuilder();
        for (String log : detailedLogs) {
            if (log.toLowerCase().contains(exceptionName.toLowerCase())) {
                stackTrace.append(log).append("\n");
            }
        }
        return stackTrace.toString().isEmpty() ? "No stack trace found for " + exceptionName : stackTrace.toString();
    }

    private List<String> extractDetailedErrorLogs(List<String> logLines) {
        List<String> detailedLogs = new ArrayList<>();
        StringBuilder currentStackTrace = new StringBuilder();  // Holds the current stack trace
        String currentLogType = "";  // Tracks the current log type being captured (ERROR, INFO, DEBUG)
        boolean isCapturing = false;  // Tracks whether we are capturing a log entry

        for (String line : logLines) {
            if (line.contains("ERROR")) {
                if (!currentLogType.equals("ERROR")) {
                    if (isCapturing) {
                        detailedLogs.add(currentStackTrace.toString().trim());
                    }
                    isCapturing = true;
                    currentStackTrace = new StringBuilder();
                    currentStackTrace.append(line).append("\n");
                    currentLogType = "ERROR";  // Update the log type
                } else if (isCapturing) {
                    currentStackTrace.append(line).append("\n");
                }
            } else if (line.contains("INFO")) {
                if (!currentLogType.equals("INFO")) {
                    if (isCapturing) {
                        detailedLogs.add(currentStackTrace.toString().trim());
                    }
                    isCapturing = true;
                    currentStackTrace = new StringBuilder();
                    currentStackTrace.append(line).append("\n");
                    currentLogType = "INFO";
                } else if (isCapturing) {
                    currentStackTrace.append(line).append("\n");
                }
            } else if (line.contains("DEBUG")) {
                if (!currentLogType.equals("DEBUG")) {
                    if (isCapturing) {
                        detailedLogs.add(currentStackTrace.toString().trim());
                    }
                    isCapturing = true;
                    currentStackTrace = new StringBuilder();
                    currentStackTrace.append(line).append("\n");
                    currentLogType = "DEBUG";
                } else if (isCapturing) {
                    currentStackTrace.append(line).append("\n");
                }
            } else if (isCapturing) {
                if (line.isEmpty() || line.startsWith("ERROR") || line.startsWith("INFO") || line.startsWith("DEBUG")) {
                    detailedLogs.add(currentStackTrace.toString().trim());
                    isCapturing = false;  // Reset capturing flag
                } else {
                    currentStackTrace.append(line).append("\n");
                }
            }
        }

        if (isCapturing) {
            detailedLogs.add(currentStackTrace.toString().trim());
        }

        return detailedLogs;
    }



    private int countExceptions(List<String> fileData) {
        int count = 0;
        for (String line : fileData) {
            if (line.toLowerCase().contains("exception")) {
                count++;
            }
        }
        return count;
    }

    private int countLogLevel(List<String> fileData, String logLevel) {
        int count = 0;
        String logLevelPrefix = logLevel.toUpperCase();

        for (String line : fileData) {
            if (line.toLowerCase().contains(logLevelPrefix.toLowerCase())) {
                count++;
            }
        }
        return count;
    }
}
