package Codify.parsing.exception;

import Codify.parsing.exception.baseException.BaseException;
import Codify.parsing.exception.databaseException.DatabaseException;
import Codify.parsing.exception.parsingException.SyntaxException;
import Codify.parsing.exception.parsingException.TokenizationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler {
    //exceptionHandler는 Spring framework 내부적으로 호출(외부에서 호출 x) -> protected 접근 제어자 사용
    //log.error로 나중에 로그 수집/분석

    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ApiErrorResponse> handle(BaseException e) {
        log.error("BusinessException", e);
        return createErrorResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiErrorResponse> handle(Exception e) {
        e.printStackTrace();
        log.error("Exception", e);
        return createErrorResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }


    //of factory method를 이용하셔 ApiErrorResponse 객체 생성
    private ResponseEntity<ApiErrorResponse> createErrorResponseEntity(ErrorCode errorCode) {
        return new ResponseEntity<>(
                ApiErrorResponse.of(errorCode),
                errorCode.getStatus());
    }

    @ExceptionHandler(SyntaxException.class)
    protected ResponseEntity<ApiErrorResponse> handle(SyntaxException e) {
        log.error("SyntaxException", e);
        return createErrorResponseEntity(e.getErrorCode());
    }


    @ExceptionHandler(TokenizationException.class)
    protected ResponseEntity<ApiErrorResponse> handle(TokenizationException e) {
        log.error("TokenizationException", e);
        return createErrorResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler(DatabaseException.class)
    protected ResponseEntity<ApiErrorResponse> handle(DatabaseException e) {
        log.error("Database error", e);
        return createErrorResponseEntity(e.getErrorCode());
    }

}
