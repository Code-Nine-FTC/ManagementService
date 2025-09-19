package com.codenine.managementservice.utils.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserSectionMismatchException.class)
    public ResponseEntity<String> handleUserSectionMismatchException(UserSectionMismatchException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
