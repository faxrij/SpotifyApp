package com.example.spotifyproject.exception;

import com.example.spotifyproject.model.response.ErrorModel;

import org.springframework.http.HttpStatus;
import org.junit.platform.commons.util.AnnotationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorModel> handleBusinessException(BusinessException businessException) {
        ErrorModel error = ErrorModel.builder()
                .statusCode(businessException.getStatusCode())
                .errorCode(businessException.getErrorCode())
                .message(businessException.getMessage())
                .build();
        return new ResponseEntity<>(error, Objects.requireNonNull(HttpStatus.resolve(businessException.getStatusCode())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorModel> handleMethodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException) {
        FieldError fieldError = methodArgumentNotValidException.getBindingResult().getFieldError();
        assert fieldError != null;
        ErrorModel error = ErrorModel.builder()
                .statusCode(400)
                .errorCode("Bad Request")
                .message(fieldError.getField()+" "+fieldError.getDefaultMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.resolve(error.getStatusCode()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorModel> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
        ErrorModel error = ErrorModel.builder()
                .statusCode(400)
                .errorCode("Bad Request")
                .message("Invalid type given")
                .build();
        return new ResponseEntity<>(error, HttpStatus.resolve(error.getStatusCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorModel> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class).isPresent())
            throw e;
        ErrorModel error = ErrorModel.builder()
                .statusCode(ErrorCode.internal_server_error.getHttpCode()) //500
                .errorCode("Error")
                .message(e.getLocalizedMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.resolve(500));
    }
}