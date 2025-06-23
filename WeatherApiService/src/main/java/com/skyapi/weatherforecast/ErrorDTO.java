package com.skyapi.weatherforecast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorDTO {
	private Date timestamp;
	private int status;
	private String path;
	private List<String> errors = new ArrayList<String>();

	public void addError(String error) {
		errors.add(error);
	}
}
