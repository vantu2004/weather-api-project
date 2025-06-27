package com.skyapi.weatherforecast.location;

/*
 * đổi từ Exception (xử lý lỗi khi dùng try/catch hoặc throw) bằng
 * RuntimeException (xử lý lỗi khi chạy) vì đã có GlobalException xử lý khi chạy
 * lỗi
 */
public class LocationNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LocationNotFoundException(String locationCode) {
		super("No location found with the given code " + locationCode);
	}

	public LocationNotFoundException(String countryCode, String cityName) {
		super("No location found with the given country code " + countryCode + " and the city name " + cityName);
	}
}
