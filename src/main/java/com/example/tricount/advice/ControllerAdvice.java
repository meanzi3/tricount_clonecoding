package com.example.tricount.advice;

import com.example.tricount.exception.ForbiddenAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {

  @ExceptionHandler(ForbiddenAccessException.class)
  public ResponseEntity<String> forbiddenAccessExceptionHandler(ForbiddenAccessException e){
    return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
  }
}
