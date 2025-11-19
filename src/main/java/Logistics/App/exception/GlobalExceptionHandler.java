package Logistics.App.exception;

import Logistics.App.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "User Already Exists",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex,
                                                                  HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid Credentials",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse>handleUserNotFound(UserNotFoundException userNotFoundException,HttpServletRequest request){
        ErrorResponse body= new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "User not found",
                userNotFoundException.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(body,HttpStatus.NOT_FOUND);
    }

}

















//
//@ExceptionHandler(MethodArgumentNotValidException.class)
//public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
//                                                      HttpServletRequest request) {
//    Map<String, String> fieldErrors = new HashMap<>();
//    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
//        fieldErrors.put(fe.getField(), fe.getDefaultMessage());
//    }
//
//    ErrorResponse body = new ErrorResponse(
//            HttpStatus.BAD_REQUEST.value(),
//            HttpStatus.BAD_REQUEST.getReasonPhrase(),
//            "Validation failed",
//            request.getRequestURI()
//    );
//    body.setFieldErrors(fieldErrors);
//
//    return new ResponseEntity<>(body, new HttpHeaders(), HttpStatus.BAD_REQUEST);
//}
//
//@ExceptionHandler(AccessDeniedException.class)
//public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
//                                                        HttpServletRequest request) {
//    ErrorResponse body = new ErrorResponse(
//            HttpStatus.FORBIDDEN.value(),
//            HttpStatus.FORBIDDEN.getReasonPhrase(),
//            "Access denied",
//            request.getRequestURI()
//    );
//    return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
//}
//
//@ExceptionHandler(Exception.class)
//public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
//    ErrorResponse body = new ErrorResponse(
//            HttpStatus.INTERNAL_SERVER_ERROR.value(),
//            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
//            "An unexpected error occurred",
//            request.getRequestURI()
//    );
//    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
//}


//@ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex,
//                                                          HttpServletRequest request) {
//        ErrorResponse body = new ErrorResponse(
//                HttpStatus.CONFLICT.value(),
//                HttpStatus.CONFLICT.getReasonPhrase(),
//                ex.getMessage(),
//                request.getRequestURI()
//        );
//        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
//    }