package com.skyapi.weatherforecast.location;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.daily.DailyWeatherApiController;
import com.skyapi.weatherforecast.full.FullWeatherApiController;
import com.skyapi.weatherforecast.hourly.HourlyWeatherApiController;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherApiController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/locations")
@RequiredArgsConstructor
@Validated
public class LocationApiController {
	private final LocationService locationService;
	private final ModelMapper modelMapper;
	private Map<String, String> propertyMap = Map.of("code", "code", "city_name", "cityName", "region_name",
			"regionName", "country_code", "countryCode", "country_name", "countryName", "enabled", "enabled");

	@PostMapping
	public ResponseEntity<?> addLocation(@RequestBody @Valid LocationDTO locationDTO) {
		Location addedLocation = this.locationService.add(this.convertLocationDTOToEntity(locationDTO));

		/*
		 * việc getCode() từ location trong trường hợp code null vẫn ko sao vì khi +
		 * chuỗi thì kết quả vẫn là "/v1/locations/null" nhưng nếu getCode() từ
		 * addedLocation sẽ gây lỗi nếu addedLocation ko thực sự tồn tại
		 */
		URI uri = URI.create("/v1/locations/" + addedLocation.getCode());

		LocationDTO addedLocationDTO = this.convertLocationEntityToDTO(addedLocation);

		/*
		 * hàm created() có tác dụng tạo thêm Location trong header, nghĩa là cho biết
		 * vị trí tài nguyên ms đc tạo nằm ở đâu
		 */
		return ResponseEntity.created(uri).body(this.addLinksByLocation(addedLocationDTO));
	}

	@Deprecated
	public ResponseEntity<?> listAllLocations() {
		List<Location> locations = this.locationService.getAllLocationUnTrashed();
		if (locations.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(this.convertListLocationEntityToDTO(locations));
	}

	@GetMapping
	public ResponseEntity<?> listAllLocations(
			@RequestParam(value = "page", required = false, defaultValue = "1") @Min(value = 1) Integer page,
			@RequestParam(value = "size", required = false, defaultValue = "5") @Min(value = 1) @Max(value = 20) Integer size,
			@RequestParam(value = "sort", required = false, defaultValue = "code") String sortOption,
			@RequestParam(value = "enabled", required = false, defaultValue = "") String enabled,
			@RequestParam(value = "region_name", required = false, defaultValue = "") String regionName,
			@RequestParam(value = "country_code", required = false, defaultValue = "") String countryCode)
			throws BadRequestException {

		sortOption = this.validateSortOption(sortOption);

		// tạo map các field được lọc
		Map<String, Object> filterFields = new HashMap<String, Object>();
		if (!"".equals(enabled)) {
			filterFields.put("enabled", Boolean.parseBoolean(enabled));
		}
		if (!"".equals(regionName)) {
			filterFields.put("regionName", regionName);
		}
		if (!"".equals(countryCode)) {
			filterFields.put("countryCode", countryCode);
		}

		/*
		 * đảm bảo biến sort truyền vào dạng snakecase (tương ứng tên field dạng json),
		 * sau đó được convert sang camelcase (giống tên field trong entity) để có thể
		 * sort (Hibernate chỉ hiểu khi sort <=> tên field trong entity)
		 */
		Page<Location> pageLocations = this.locationService.getAllLocationUnTrashedWithFilter(page - 1, size,
				sortOption, filterFields);
		List<Location> listLocations = pageLocations.getContent();

		if (listLocations.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		List<LocationDTO> locationDTOs = this.convertListLocationEntityToDTO(listLocations);

		return ResponseEntity.ok(this.addPageMetaDataAndLinksToCollection(pageLocations, locationDTOs, sortOption,
				enabled, regionName, countryCode));
	}

	private String validateSortOption(String sortOption) throws BadRequestException {
		/*
		 * String là immutable, sau khi biến đổi mà lại gán trực tiếp lại cho sortOption
		 * có khi gây lỗi vì đổi dữ liệu gốc, hàm replace sẽ trả về chuỗi mới -> dùng
		 * biến mới cho an toàn
		 */
		String translatedSortOption = sortOption;

		String[] sortFields = sortOption.split(",");
		if (sortFields.length > 1) {
			for (String sortField : sortFields) {
				String actualSortField = sortField.replace("-", "");
				if (!propertyMap.containsKey(actualSortField)) {
					throw new BadRequestException("Invalid sort field: " + sortOption);
				}

				translatedSortOption = translatedSortOption.replace(actualSortField, propertyMap.get(actualSortField));
			}
		} else {
			// giả sử sortOption là "-region_name" -> actualSortField là "region_name"
			String actualSortField = sortOption.replace("-", "");
			if (!propertyMap.containsKey(actualSortField)) {
				throw new BadRequestException("Invalid sort field: " + sortOption);
			}

			/*
			 * chuẩn hóa lại sortOption (snakecase) theo propertyMap (camelcase) ->
			 * sortOption sẽ là -regionName, bên service có xử lý riêng cho sortOption rồi
			 */
			translatedSortOption = sortOption.replace(actualSortField, propertyMap.get(actualSortField));
		}

		return translatedSortOption;
	}

	/*
	 * vì trả về list nên buộc dùng CollectionModel để nhúng listDTO trong
	 * _embedded, còn nếu trả về object đơn thì chỉ cần dùng EntityModel (extends từ
	 * RepresentationModel) hoặc cho DTO extends trực tiếp từ RepresentationModel
	 */
	private CollectionModel<LocationDTO> addPageMetaDataAndLinksToCollection(Page<Location> pageLocations,
			List<LocationDTO> locationDTOs, String sortField, String enabled, String regionName, String countryCode)
			throws BadRequestException {
		String actualEnabled = "".equals(enabled) ? null : enabled;
		String actualRegionName = "".equals(regionName) ? null : regionName;
		String actualCountryCode = "".equals(countryCode) ? null : countryCode;

		// add _links cho riêng từng DTO
		for (LocationDTO locationDTO : locationDTOs) {
			locationDTO.add(
					linkTo(methodOn(LocationApiController.class).getLocation(locationDTO.getCode())).withSelfRel());
		}

		int pageSize = pageLocations.getSize();

		// pageNum + 1 vì index chạy từ 0
		int pageNum = pageLocations.getNumber() + 1;
		long totalElements = pageLocations.getTotalElements();
		int totalPages = pageLocations.getTotalPages();

		// PageMetaData chứa thông tin phân trang
		PageMetadata pageMetadata = new PageMetadata(pageSize, pageNum, totalElements, totalPages);

		/*
		 * CollectionModel bọc ListDTO + _links, PageModel extends CollectionModel giúp
		 * bọc thêm page -> đáng lẽ là trả về PagedModel nhưng trả về CollectionModel để
		 * tăng tính trừu tượng, linh hoạt và tái sử dụng (vd: trả về
		 * CollectionModel(listDTOs + _links) hoặc PagedModel(listDTOs + _links + page))
		 * 
		 * PagedModel kế thừa từ CollectionModel, theo tính đa hình, ta có thể khai báo
		 * một biến có kiểu CollectionModel và gán cho nó một đối tượng thực sự là
		 * PagedModel. Nhưng nếu muốn truy cập phương thức đặc trưng của PagedModel thì
		 * phải ép kiểu
		 * 
		 * tính đa hình <=> 1 lớp có thể có nhiều hình thái (gồm chính nó hoặc các lớp
		 * con)
		 */
		CollectionModel<LocationDTO> collectionModel = PagedModel.of(locationDTOs, pageMetadata);

		// add _links cho collectionModel
		collectionModel.add(linkTo(methodOn(LocationApiController.class).listAllLocations(pageNum, pageSize, sortField,
				actualEnabled, actualRegionName, actualCountryCode)).withSelfRel());

		// nếu pageNum > 1 thì trả về firstLink và prevLink
		if (pageNum > 1) {
			collectionModel.add(linkTo(methodOn(LocationApiController.class).listAllLocations(1, pageSize, sortField,
					actualEnabled, actualRegionName, actualCountryCode)).withRel(IanaLinkRelations.FIRST));
			collectionModel.add(linkTo(methodOn(LocationApiController.class).listAllLocations(pageNum - 1, pageSize,
					sortField, actualEnabled, actualRegionName, actualCountryCode)).withRel(IanaLinkRelations.PREV));
		}

		// nếu vẫn còn trang tiếp theo thì thêm next và last
		if (pageNum < pageLocations.getTotalPages()) {
			collectionModel.add(linkTo(methodOn(LocationApiController.class).listAllLocations(pageNum + 1, pageSize,
					sortField, actualEnabled, actualRegionName, actualCountryCode)).withRel(IanaLinkRelations.NEXT));
			collectionModel
					.add(linkTo(methodOn(LocationApiController.class).listAllLocations(pageLocations.getTotalPages(),
							pageSize, sortField, actualEnabled, actualRegionName, actualCountryCode))
							.withRel(IanaLinkRelations.LAST));
		}

		return collectionModel;
	}

	private LocationDTO addLinksByLocation(LocationDTO locationDTO) {
		locationDTO.add(linkTo(methodOn(LocationApiController.class).getLocation(locationDTO.getCode())).withSelfRel());
		locationDTO.add(
				linkTo(methodOn(RealtimeWeatherApiController.class).getRealtimeByLocationCode(locationDTO.getCode()))
						.withRel("realtime"));
		locationDTO.add(linkTo(methodOn(HourlyWeatherApiController.class)
				.listHourlyForecastByLocationCode(locationDTO.getCode(), null)).withRel("hourly_forecast"));
		locationDTO.add(
				linkTo(methodOn(DailyWeatherApiController.class).getDailyForecastByLocationCode(locationDTO.getCode()))
						.withRel("daily_forecast"));
		locationDTO.add(
				linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByLocationCode(locationDTO.getCode()))
						.withRel("full_forecast"));

		return locationDTO;
	}

	@GetMapping("/{code}")
	public ResponseEntity<?> getLocation(@PathVariable("code") String code) {
		Location location = this.locationService.getLocationByCode(code);
		LocationDTO locationDTO = this.convertLocationEntityToDTO(location);

		return ResponseEntity.ok(this.addLinksByLocation(locationDTO));
	}

	@PutMapping
	public ResponseEntity<?> updateLocation(@RequestBody @Valid LocationDTO locationDTO) {
		Location location = this.locationService.updateLocation(this.convertLocationDTOToEntity(locationDTO));
		LocationDTO updatedLocationDTO = this.convertLocationEntityToDTO(location);

		return ResponseEntity.ok(this.addLinksByLocation(updatedLocationDTO));
	}

	@DeleteMapping("/{code}")
	public ResponseEntity<?> deleteLocation(@PathVariable("code") String code) {
		this.locationService.deleteLocation(code);
		return ResponseEntity.noContent().build();
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
