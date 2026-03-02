package github.maxsuel.agregadordeinvestimentos.exceptions.handler;

import github.maxsuel.agregadordeinvestimentos.exceptions.*;
import github.maxsuel.agregadordeinvestimentos.exceptions.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation error captured: {}", errors);

        var errorResponse = new ErrorResponseDto(
                status.value(),
                "Validation failed for one or more fields",
                Instant.now(),
                errors
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ErrorResponseDto> handleAuthErrors(@NonNull Exception ex) {
        log.warn("Authentication failure: {}", ex.getMessage());

        var errorResponse = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid username or password",
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(@NonNull IllegalArgumentException ex) {
        log.error("Business logic error: {}", ex.getMessage());

        var errorResponse = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                Instant.now(),
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(@NonNull AccessDeniedException ex) {
        var errorResponse = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                "Access denied: You do not have permission for this.",
                Instant.now(),
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralException(Exception ex) {
        log.error("Unexpected critical error: ", ex);

        var errorResponse = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected internal error occurred on the server.",
                Instant.now(),
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DuplicatedDataException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicatedData(@NonNull DuplicatedDataException ex) {
        var errorResponse = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler({InsufficientFundsException.class, InsufficientSharesException.class})
    public ResponseEntity<ErrorResponseDto> handleBusinessLogicErrors(@NonNull Exception ex) {
        var errorResponse = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler({AccountNotFoundException.class, StockNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<ErrorResponseDto> handleNotFound(@NonNull Exception ex) {
        var errorResponse = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponseDto> handleFileStorage(@NonNull FileStorageException ex) {
        log.error("Storage error: {}", ex.getMessage());

        var errorResponse = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
