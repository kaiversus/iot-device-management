package com.iot.management.exception;

import com.iot.management.controller.*;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Order(-10)
@ControllerAdvice(assignableTypes = {DeviceController.class, DeviceWebController.class,
    CategoryController.class, CategoryWebController.class})
public class DeviceExceptionHandler {
    @ExceptionHandler(DeviceException.class)
    public Object domain(DeviceException ex, HttpServletRequest request) { return error(ex.getStatus(), ex.getMessage(), request); }
    @ExceptionHandler(AccessDeniedException.class)
    public Object forbidden(HttpServletRequest request) { return error(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này", request); }
    @ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
    public Object conflict(HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "Dữ liệu bị trùng, đang được sử dụng hoặc vừa được thay đổi. Vui lòng tải lại trang.", request);
    }
    @ExceptionHandler(BindException.class)
    public Object validation(BindException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
        if (request.getRequestURI().startsWith(request.getContextPath() + "/api/"))
            return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu không hợp lệ", "fields", fields));
        return error(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại biểu mẫu.", request);
    }
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class})
    public Object badRequest(HttpServletRequest request) { return error(HttpStatus.BAD_REQUEST, "Tham số hoặc dữ liệu gửi lên không hợp lệ", request); }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object tooLarge(HttpServletRequest request) { return error(HttpStatus.PAYLOAD_TOO_LARGE, "Ảnh vượt quá giới hạn 5 MB", request); }
    private Object error(HttpStatus status, String message, HttpServletRequest request) {
        if (request.getRequestURI().startsWith(request.getContextPath() + "/api/"))
            return ResponseEntity.status(status).body(Map.of("error", message));
        ModelAndView view = new ModelAndView("devices/error");
        view.setStatus(status); view.addObject("status", status.value()); view.addObject("message", message); return view;
    }
}
