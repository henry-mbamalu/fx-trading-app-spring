package com.app.fxtradingapp.exception;

import com.app.fxtradingapp.dto.ResponseDto;
import com.app.fxtradingapp.util.LocaleHandler;
import com.app.fxtradingapp.util.ResponseCodes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ResponseDto response = new ResponseDto();
        response.setData(errors);
        response.setMessage("Validation error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
