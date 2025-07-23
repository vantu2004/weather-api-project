package com.skyapi.weatherforecast.location;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

	// bỏ public để mặc định là default - chỉ dùng trong package
	@Deprecated
	List<Location> getAllLocationUnTrashed() {
		return this.locationRepository.findAllUnTrashed();
	}

	public Page<Location> getAllLocationUnTrashed(Integer pageNum, Integer pageSize, String sortField) {
		Sort sort = Sort.by(sortField).ascending();
		Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

		return this.locationRepository.findAllUnTrashed(pageable);
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
