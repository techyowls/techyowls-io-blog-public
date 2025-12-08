package io.techyowls.webflux.config;

import io.techyowls.webflux.service.ProductService.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

/**
 * Error handling in WebFlux.
 * Returns Mono<ProblemDetail> for reactive compatibility.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<ProblemDetail> handleNotFound(ProductNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Product Not Found");
        problem.setDetail(ex.getMessage());
        return Mono.just(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ProblemDetail> handleBadRequest(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid Request");
        problem.setDetail(ex.getMessage());
        return Mono.just(problem);
    }
}
