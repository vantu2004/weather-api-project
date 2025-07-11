package com.skyapi.weatherforecast;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.skyapi.weatherforecast.location.LocationNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
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
		errorDTO.addError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		errorDTO.setPath(request.getServletPath());

		LOGGER.error(ex.getMessage(), ex);

		return errorDTO;
	}

	@ExceptionHandler({BadRequestException.class, GeolocationException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorDTO handlerBadRequestException(HttpServletRequest request, Exception ex) {
		ErrorDTO errorDTO = new ErrorDTO();
		errorDTO.setTimestamp(new Date());
		errorDTO.setStatus(HttpStatus.BAD_REQUEST.value());
		errorDTO.addError(ex.getMessage());
		errorDTO.setPath(request.getServletPath());

		LOGGER.error(ex.getMessage(), ex);

		return errorDTO;
	}

	// bắt lỗi validate cho List<HourlyWeatherDTO> bên HourlyWeatherApiController
	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorDTO handlerConstrainViolationException(HttpServletRequest request, Exception ex) {
		ConstraintViolationException constraintViolationException = (ConstraintViolationException) ex;

		ErrorDTO errorDTO = new ErrorDTO();
		errorDTO.setTimestamp(new Date());
		errorDTO.setStatus(HttpStatus.BAD_REQUEST.value());

		Set<ConstraintViolation<?>> constraintViolations = constraintViolationException.getConstraintViolations();
		constraintViolations.forEach(constraintViolation -> errorDTO
				.addError(constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage()));

		errorDTO.setPath(request.getServletPath());

		LOGGER.error(ex.getMessage(), ex);

		return errorDTO;
	}

	// refactor code cho LocationNotFoundException
	@ExceptionHandler(LocationNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public ErrorDTO handlerLocationNotFoundException(HttpServletRequest request, Exception ex) {
		ErrorDTO errorDTO = new ErrorDTO();
		errorDTO.setTimestamp(new Date());
		errorDTO.setStatus(HttpStatus.NOT_FOUND.value());
		errorDTO.addError(ex.getMessage());
		errorDTO.setPath(request.getServletPath());

		LOGGER.error(ex.getMessage(), ex);

		return errorDTO;
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ErrorDTO errorDTO = new ErrorDTO();
		errorDTO.setTimestamp(new Date());
		errorDTO.setStatus(HttpStatus.BAD_REQUEST.value());
		errorDTO.setPath(((ServletWebRequest) request).getRequest().getServletPath());

		List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
		fieldErrors.forEach(fieldError -> {
			errorDTO.addError(fieldError.getDefaultMessage());
		});

		LOGGER.error(ex.getMessage(), ex);

		return new ResponseEntity<>(errorDTO, headers, status);
	}

}
