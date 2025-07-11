package com.skyapi.weatherforecast;

import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationRepository;

public abstract class AbstractLocationService {
	protected LocationRepository locationRepository;

	public Location getLocationByCode(String code) {
		Location location = this.locationRepository.findByCode(code);
		if (location == null) {
			throw new LocationNotFoundException(code);
		}
		return location;
	}
}
