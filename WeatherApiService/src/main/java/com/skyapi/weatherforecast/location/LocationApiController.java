package com.skyapi.weatherforecast.location;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skyapi.weatherforecast.common.Location;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/locations")
@RequiredArgsConstructor
public class LocationApiController {
	private final LocationService locationService;

	@PostMapping
	public ResponseEntity<Location> addLocation(@RequestBody @Valid Location location) {
		Location addedLocation = this.locationService.add(location);

		/*
		 * việc getCode() từ location trong trường hợp code null vẫn ko sao vì khi +
		 * chuỗi thì kết quả vẫn là "/v1/locations/null" nhưng nếu getCode() từ
		 * addedLocation sẽ gây lỗi nếu addedLocation ko thực sự tồn tại
		 */
		URI uri = URI.create("/v1/locations/" + addedLocation.getCode());

		/*
		 * hàm created() có tác dụng tạo thêm Location trong header, nghĩa là cho biết
		 * vị trí tài nguyên ms đc tạo nằm ở đâu
		 */
		return ResponseEntity.created(uri).body(addedLocation);
	}

	@GetMapping
	public ResponseEntity<?> listAllLocations() {
		List<Location> locations = this.locationService.getAllLocationUnTrashed();
		if (locations.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(locations);
	}

	@GetMapping("/{code}")
	public ResponseEntity<?> getLocation(@PathVariable("code") String code) {
		Location location = this.locationService.getLocationByCode(code);
		if (location == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(location);
	}

	@PutMapping
	public ResponseEntity<?> updateLocation(@RequestBody @Valid Location locationInRequest) {
		try {
			Location location = this.locationService.updateLocation(locationInRequest);
			return ResponseEntity.ok(location);
		} catch (LocationNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{code}")
	public ResponseEntity<?> deleteLocation(@PathVariable("code") String code) {
		try {
			this.locationService.deleteLocation(code);
			return ResponseEntity.noContent().build();
		} catch (LocationNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}
}
