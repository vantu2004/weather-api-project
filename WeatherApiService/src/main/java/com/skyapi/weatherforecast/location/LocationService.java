package com.skyapi.weatherforecast.location;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.skyapi.weatherforecast.AbstractLocationService;
import com.skyapi.weatherforecast.common.Location;

@Service
@EnableCaching
public class LocationService extends AbstractLocationService {

	public LocationService(LocationRepository locationRepository) {
		super();
		// locationRepository kế thừa từ AbstractLocationService
		this.locationRepository = locationRepository;
	}

	// Luôn chạy method và cập nhật cache với kết quả mới.
	@CachePut(cacheNames = "locationCacheByCode", key = "#location.code")
	/*
	 * sau khi thêm location mới thì xóa hết key trong cache
	 * locationCacheByPagination
	 */
	@CacheEvict(cacheNames = "locationCacheByPagination", allEntries = true)
	public Location add(Location location) {
		return this.locationRepository.save(location);
	}

	// bỏ public để mặc định là default - chỉ dùng trong package
	@Deprecated
	List<Location> getAllLocationUnTrashed() {
		return this.locationRepository.findAllUnTrashed();
	}

	@Deprecated
	public Page<Location> getAllLocationUnTrashed(Integer pageNum, Integer pageSize, String sortField) {
		Sort sort = Sort.by(sortField).ascending();
		Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

		return this.locationRepository.findAllUnTrashed(pageable);
	}

	/*
	 * lưu với cache tên locationCacheByPagination, trong cache này lấy hết các tham
	 * số đầu vào làm key
	 */
	@Cacheable("locationCacheByPagination")
	public Page<Location> getAllLocationUnTrashedWithFilter(Integer pageNum, Integer pageSize, String sortOption,
			Map<String, Object> filterFields) {

		// tránh null
		Sort sort = Sort.unsorted();

		String[] sortFields = sortOption.split(",");
		for (String sortField : sortFields) {
			String actualSortField = sortField.replace("-", "");
			Sort tempSort = sortField.startsWith("-") ? Sort.by(actualSortField).descending()
					: Sort.by(actualSortField).ascending();
			sort = sort.and(tempSort);
		}

		Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
		return this.locationRepository.listWithFilter(pageable, filterFields);
	}

	/*
	 * cập nhật dữ liệu trong cache locationCacheByCode và tất cả dữ liệu trong
	 * cache locationCacheByPagination
	 */
	@CachePut(cacheNames = "locationCacheByCode", key = "#locationInRequest.code")
	@CacheEvict(cacheNames = "locationCacheByPagination", allEntries = true)
	public Location updateLocation(Location locationInRequest) {
		String code = locationInRequest.getCode();
		Location locationInDb = this.getLocationByCode(code);

		// reafactor để bên updateFullWeather dùng chung
		locationInDb.copyFieldsFrom(locationInRequest);

		return this.locationRepository.save(locationInDb);
	}

	// xóa dữ liệu đồng thời trong 2 cache
	@CacheEvict(cacheNames = { "locationCacheByCode", "locationCacheByPagination" }, allEntries = true)
	public void deleteLocation(String code) {
		this.getLocationByCode(code);
		this.locationRepository.trashByCode(code);
	}
}
