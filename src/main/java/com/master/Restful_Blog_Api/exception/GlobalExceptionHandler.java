package com.master.Restful_Blog_Api.exception;

import com.master.Restful_Blog_Api.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException ex,
            HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseDTO> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN,
                "Access denied: You don't have permission to access this resource",
                request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handlePostNotFound(
            PostNotFoundException ex,
            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCommentNotFound(
            CommentNotFoundException ex,
            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CommentNotBelongsToPostException.class)
    public ResponseEntity<ErrorResponseDTO> handleCommentNotBelongsToPost(
            CommentNotBelongsToPostException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(PostWithoutAuthorException.class)
    public ResponseEntity<ErrorResponseDTO> handleCommentNotBelongsToPost(
            PostWithoutAuthorException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleGlobalExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                        ? fieldError.getDefaultMessage()
                        : "Invalid value",
                        // keep first error if the same field has multiple violations
                        (existing, duplicate) -> existing
                ));

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", "Validation failed");
        response.put("errors", errors);
        response.put("path", request.getRequestURI());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request);
    }

    /// Helper Method
    private ResponseEntity<ErrorResponseDTO> build(
            HttpStatus status, String message, HttpServletRequest request) {

        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .status(status.value())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
