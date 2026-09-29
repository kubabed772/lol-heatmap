package pl.jbedlinski.heatmap.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.jbedlinski.heatmap.riot.RateLimitException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record RateLimitResponse(long retryAfter) {}

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<RateLimitResponse> handleRateLimit(RateLimitException ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new RateLimitResponse(ex.getRetryAfter()));
    }
}