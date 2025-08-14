package com.skyapi.weatherforecast;

import org.springframework.cache.annotation.Cacheable;

import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationRepository;

public abstract class AbstractLocationService {
	protected LocationRepository locationRepository;

	// Nếu cache đã có → lấy dữ liệu, nếu chưa → chạy method và lưu vào cache.
	@Cacheable(cacheNames = "locationCacheByCode", key = "#code")
	public Location getLocationByCode(String code) {
		Location location = this.locationRepository.findByCode(code);
		if (location == null) {
			throw new LocationNotFoundException(code);
		}
		return location;
	}
}
