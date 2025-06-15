package com.skyapi.weatherforecast;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	// mặc định trả về tên view, nhưng @ResponseBody bắt trả về dữ liệu JSON
	@ResponseBody
	public ErrorDTO handlerGenericException(HttpServletRequest request, Exception ex) {
		ErrorDTO errorDTO = new ErrorDTO();
		errorDTO.setTimestamp(new Date());
		errorDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		// trả về lỗi ngắn gọn "Internal Server Error"
		errorDTO.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		errorDTO.setPath(request.getServletPath());

		LOGGER.error(ex.getMessage(), ex);

		return errorDTO;
	}
}
