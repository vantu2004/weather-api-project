package com.skyapi.weatherforecast.location;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skyapi.weatherforecast.AbstractLocationService;
import com.skyapi.weatherforecast.common.Location;

@Service
public class LocationService extends AbstractLocationService {

	public LocationService(LocationRepository locationRepository) {
		super();
		this.locationRepository = locationRepository;
	}

	public Location add(Location location) {
		return this.locationRepository.save(location);
	}

	public List<Location> getAllLocationUnTrashed() {
		return this.locationRepository.findAllUnTrashed();
	}

	public Location updateLocation(Location locationInRequest) {
		String code = locationInRequest.getCode();
		Location locationInDb = this.getLocationByCode(code);

		// reafactor để bên updateFullWeather dùng chung
		locationInDb.copyFieldsFrom(locationInRequest);

		return this.locationRepository.save(locationInDb);
	}

	public void deleteLocation(String code) {
		this.getLocationByCode(code);
		this.locationRepository.trashByCode(code);
	}
}
