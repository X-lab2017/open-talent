package com.example.backend.exception;

/**
 * GitHub API异常类
 */
public class GitHubApiException extends RuntimeException {
    
    private final int statusCode;
    private final String responseBody;
    
    public GitHubApiException(String message) {
        super(message);
        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public GitHubApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public GitHubApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
    
    public GitHubApiException(String message, int statusCode, String responseBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(": ").append(getMessage());
        if (statusCode > 0) {
            sb.append(" (HTTP ").append(statusCode).append(")");
        }
        if (responseBody != null && !responseBody.isEmpty()) {
            sb.append(" - Response: ").append(responseBody);
        }
        return sb.toString();
    }
}