package com.skyapi.weatherforecast.location;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
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
	private final ModelMapper modelMapper;

	@PostMapping
	public ResponseEntity<?> addLocation(@RequestBody @Valid LocationDTO locationDTO) {
		Location addedLocation = this.locationService.add(this.convertLocationDTOToEntity(locationDTO));

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
		return ResponseEntity.created(uri).body(this.convertLocationEntityToDTO(addedLocation));
	}

	@GetMapping
	public ResponseEntity<?> listAllLocations() {
		List<Location> locations = this.locationService.getAllLocationUnTrashed();
		if (locations.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(this.convertListLocationEntityToDTO(locations));
	}

	@GetMapping("/{code}")
	public ResponseEntity<?> getLocation(@PathVariable("code") String code) {
		Location location = this.locationService.getLocationByCode(code);
		if (location == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(this.convertLocationEntityToDTO(location));
	}

	@PutMapping
	public ResponseEntity<?> updateLocation(@RequestBody @Valid LocationDTO locationDTO) {
		try {
			Location location = this.locationService.updateLocation(this.convertLocationDTOToEntity(locationDTO));
			return ResponseEntity.ok(this.convertLocationEntityToDTO(location));
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

	private Location convertLocationDTOToEntity(@Valid LocationDTO locationDTO) {
		return modelMapper.map(locationDTO, Location.class);
	}

	private LocationDTO convertLocationEntityToDTO(Location addedLocation) {
		return modelMapper.map(addedLocation, LocationDTO.class);
	}

	private List<LocationDTO> convertListLocationEntityToDTO(List<Location> locations) {
		/*
		 * tạo 1 stream, stream sẽ dùng map() loop qua từng phần tử trong list, sau đó
		 * gọi hàm xử lý và tạo ra 1 stream mới chứa DTO, cuối cùng là ép stream về list
		 */
		return locations.stream().map(location -> this.convertLocationEntityToDTO(location))
				.collect(Collectors.toList());
	}
}
