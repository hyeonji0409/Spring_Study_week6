package com.artineer.spring_study_week2.handler;

import com.artineer.spring_study_week2.dto.ArticleDto;
import com.artineer.spring_study_week2.dto.Response;
import com.artineer.spring_study_week2.exception.ApiException;
import com.artineer.spring_study_week2.vo.ApiCode;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public Response<String> apiException(ApiException e){
        return Response.<String>builder().code(e.getCode()).data(e.getMessage()).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<String> methodArgumentNotValidException(MethodArgumentNotValidException e){
        return Response.<String>builder().code(ApiCode.BAD_REQUEST).data(e.getMessage()).build();
    }

    // @RequestParam 데이터 검증 실패시 발생
    @ExceptionHandler(ConstraintViolationException.class)
    public Response<String> constraintViolationException(ConstraintViolationException e) {
        return Response.<String>builder().code(ApiCode.BAD_REQUEST).data(e.getMessage()).build();
    }
}


