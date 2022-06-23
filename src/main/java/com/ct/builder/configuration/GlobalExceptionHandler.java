package com.ct.builder.configuration;

import java.security.InvalidParameterException;
import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // @ExceptionHandler(InvalidParameterException.class)
  // @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  // public ErrorMessage bindInvalidParameterExceptionHandler(InvalidParameterException e) {
  //   log.error("錯誤的請求，參數無效：", e);
  //   return new ErrorMessage(
  //       OffsetDateTime.now(),
  //       HttpStatus.BAD_REQUEST.value(),
  //       null,
  //       String.format("錯誤的請求，參數無效：%s", e.getMessage()),
  //       null,
  //       null);
  // }

  // @ExceptionHandler(value = NotFoundException.class)
  // @ResponseStatus(code = HttpStatus.NOT_FOUND)
  // public ErrorMessage bindNotFoundExceptionHandler(NotFoundException e) {
  //   log.error("資源未找到：", e);
  //   return new ErrorMessage(
  //       OffsetDateTime.now(),
  //       HttpStatus.NOT_FOUND.value(),
  //       null,
  //       String.format("資源未找到：%s", e.getMessage()),
  //       null,
  //       null);
  // }


  
}
