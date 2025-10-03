package com.wipro.demp.exception;
 
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.stream.Collectors;
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
 
@RestControllerAdvice
public class GlobalExceptionHandler {
 
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
 
    // @ExceptionHandler(EventNotFoundException.class)
    // public ResponseEntity<String> handleEventNotFoundException(EventNotFoundException ex){
    //     return ResponseEntity.badRequest().body(ex.getMessage());
    // }
 
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex){
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
 
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex){
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
 
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Map<String, String>> handleInvalidParam(InvalidParameterException ex) {
        return ResponseEntity.badRequest().body(
            Map.of("error", "Invalid parameter", "details", ex.getMessage())
        );
    }
 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
            .stream().map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining("; "));
 
        return ResponseEntity.badRequest().body(Map.of("error", "Validation failed", "details", message));
    }
 
    
}