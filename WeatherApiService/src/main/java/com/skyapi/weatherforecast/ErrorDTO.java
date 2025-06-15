package com.skyapi.weatherforecast;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorDTO {
	private Date timestamp;
	private int status;
	private String path;
	private String error;
}
