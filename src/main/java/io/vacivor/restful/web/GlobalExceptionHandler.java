package io.vacivor.restful.web;

import io.vacivor.restful.common.exception.BadRequestException;
import io.vacivor.restful.common.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ProblemDetail> handleBadRequest(
      BadRequestException ex, HttpServletRequest request) {
    return problem(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ProblemDetail> handleNotFound(
      NotFoundException ex, HttpServletRequest request) {
    return problem(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    String message = "Invalid parameter: " + ex.getName();
    return problem(HttpStatus.BAD_REQUEST, message, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGeneric(Exception ex, HttpServletRequest request) {
    log.error(ex.getMessage(), ex);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request);
  }

  private ResponseEntity<ProblemDetail> problem(
      HttpStatus status, String detail, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(status.getReasonPhrase());
    problem.setType(java.net.URI.create("about:blank"));
    problem.setInstance(java.net.URI.create(request.getRequestURI()));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    return new ResponseEntity<>(problem, headers, status);
  }
}
