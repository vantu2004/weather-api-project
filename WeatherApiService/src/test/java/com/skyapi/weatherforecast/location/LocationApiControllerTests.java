package com.skyapi.weatherforecast.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.SecurityConfigForControllerTests;
import com.skyapi.weatherforecast.common.Location;

// chỉ test tầng controller
@WebMvcTest(LocationApiController.class)
@Import(SecurityConfigForControllerTests.class)
@ActiveProfiles("test")
public class LocationApiControllerTests {
	private static final String REQUEST_CONTENT_TYPE = "application/json";
	private static final String END_POINT_PATH = "/v1/locations";
	private static final String RESPONSE_CONTENT_TYPE = "application/hal+json";

	// giả lập HTTP request đến controller
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private LocationService locationService;

	@Test
	public void testAddShouldReturn400BadRequest() throws Exception {
		Location location = new Location();

		String bodyContent = objectMapper.writeValueAsString(location);

		mockMvc.perform(post(END_POINT_PATH).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
				.andExpect(status().isBadRequest()).andDo(print());
	}

	@Test
	public void testAddShouldReturn201Created() throws Exception {
		String locationCode = "HCM_VN";

		Location location = new Location();
		location.setCode(locationCode);
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Southern Vietnam");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");

		LocationDTO locationDTO = new LocationDTO();
		locationDTO.setCode(location.getCode());
		locationDTO.setCityName(location.getCityName());
		locationDTO.setRegionName(location.getRegionName());
		locationDTO.setCountryName(location.getCountryName());
		locationDTO.setCountryCode(location.getCountryCode());

		Mockito.when(this.locationService.add(location)).thenReturn(location);

		String bodyContent = objectMapper.writeValueAsString(locationDTO);

		/*
		 * khi call đến api thì đag truyền bodyContent và được convert về location
		 * bởi @RequestBody (1 đối tượng mới có cùng nội dung), nhưng Mockito phía trên
		 * nhận vào tham số là location gốc, 2 location lúc này đã khác địa chỉ ô nhớ
		 * nên xảy ra lỗi vì thế cần hàm hashCode và equal để so sánh
		 */
		mockMvc.perform(post(END_POINT_PATH).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
				.andExpect(status().isCreated()).andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
				.andExpect(jsonPath("$._links.self.href", is("http://localhost" + END_POINT_PATH + "/" + locationCode)))
				.andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime/" + locationCode)))
				.andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
				.andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
				.andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
				.andDo(print());
	}

	@Test
	@Disabled
	public void testGetLocationsShouldReturn204NoContent() throws Exception {
		/*
		 * Collection là interface mà các List/Set/Map kế thừa, còn Collections chứa các
		 * phương thức thao tác với List/Set/Map
		 */

		/*
		 * giả định khi phương thức getAllLocationUnTrashed này đc gọi đến thì trả về
		 * list rỗng
		 */
		Mockito.when(this.locationService.getAllLocationUnTrashedWithFilter(Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyString(), Mockito.anyMap())).thenReturn(Page.empty());

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isNoContent()).andDo(print());
	}

	@Test
	public void testGetLocationsByPageShouldReturn204NoContent() throws Exception {
		Mockito.when(this.locationService.getAllLocationUnTrashedWithFilter(Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyString(), Mockito.anyMap())).thenReturn(Page.empty());

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isNoContent()).andDo(print());
	}

	@Test
	@Disabled
	public void testGetLocationsShouldReturn200Ok() throws Exception {
		Location location1 = new Location();
		location1.setCode("HCM_VN");
		location1.setCityName("Ho Chi Minh City");
		location1.setRegionName("Southern Vietnam");
		location1.setCountryName("Vietnam");
		location1.setCountryCode("VN");
		location1.setEnabled(true);

		Location location2 = new Location();
		location2.setCode("DN_VN");
		location2.setCityName("Da Nang");
		location2.setRegionName("Central Vietnam");
		location2.setCountryName("Vietnam");
		location2.setCountryCode("VN");
		location2.setEnabled(true);

		Mockito.when(this.locationService.getAllLocationUnTrashed()).thenReturn(List.of(location1, location2));

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isOk()).andDo(print());

	}

	@Test
	public void testGetLocationsByPageShouldReturn200Ok() throws Exception {
		Location location1 = new Location();
		location1.setCode("HCM_VN");
		location1.setCityName("Ho Chi Minh City");
		location1.setRegionName("Southern Vietnam");
		location1.setCountryName("Vietnam");
		location1.setCountryCode("VN");
		location1.setEnabled(true);

		Location location2 = new Location();
		location2.setCode("DN_VN");
		location2.setCityName("Da Nang");
		location2.setRegionName("Central Vietnam");
		location2.setCountryName("Vietnam");
		location2.setCountryCode("VN");
		location2.setEnabled(true);

		List<Location> listLocations = List.of(location1, location2);

		Integer pageNum = 1;
		Integer pageSize = 5;
		String sortField = "code";
		Integer totalElement = listLocations.size();

		Sort sort = Sort.by(sortField).ascending();

		/*
		 * constructor PageImpl ko xử lý giảm pageNum xuống 1 -> lỗi logic (do đang xử
		 * lý bên service là pageNum - 1 trong getAllLocationUnTrashed())
		 */
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

		// giả lập paging
		Page<Location> pageLocations = new PageImpl<Location>(listLocations, pageable, totalElement);

		String requestURI = END_POINT_PATH + "?page=" + pageNum + "&size=" + pageSize + "&sort=" + sortField;

		Mockito.when(this.locationService.getAllLocationUnTrashedWithFilter(Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyString(), Mockito.anyMap())).thenReturn(pageLocations);

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(content().contentType("application/hal+json"))
				.andExpect(header().string("Expires", not("0")))
				.andExpect(jsonPath("_embedded.locations[0].code", is("HCM_VN")))
				.andExpect(jsonPath("_embedded.locations[0].city_name", is("Ho Chi Minh City")))
				.andExpect(jsonPath("_embedded.locations[1].code", is("DN_VN")))
				.andExpect(jsonPath("_embedded.locations[1].city_name", is("Da Nang")))
				.andExpect(jsonPath("page.number", is(pageNum))).andExpect(jsonPath("page.size", is(pageSize)))
				.andExpect(jsonPath("page.total_elements", is(totalElement)))
				.andExpect(jsonPath("page.total_pages", is(pageLocations.getTotalPages()))).andDo(print());

	}

	// đảm bảo _links chỉ trả về self
	@Test
	public void testPaginationLinksOnlyOnePage() throws Exception {
		Location location1 = new Location();
		location1.setCode("HCM_VN");
		location1.setCityName("Ho Chi Minh City");
		location1.setRegionName("Southern Vietnam");
		location1.setCountryName("Vietnam");
		location1.setCountryCode("VN");
		location1.setEnabled(true);

		Location location2 = new Location();
		location2.setCode("DN_VN");
		location2.setCityName("Da Nang");
		location2.setRegionName("Central Vietnam");
		location2.setCountryName("Vietnam");
		location2.setCountryCode("VN");
		location2.setEnabled(true);

		List<Location> listLocations = List.of(location1, location2);

		int pageSize = 5;
		int pageNum = 1;
		String sortField = "code";
		int totalElements = listLocations.size();

		Sort sort = Sort.by(sortField);
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);
		Page<Location> page = new PageImpl<>(listLocations, pageable, totalElements);

		Mockito.when(this.locationService.getAllLocationUnTrashedWithFilter(Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyString(), Mockito.anyMap())).thenReturn(page);

		String hostName = "http://localhost";
		String requestURI = END_POINT_PATH + "?page=" + pageNum + "&size=" + pageSize + "&sort=" + sortField;

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
				.andExpect(jsonPath("$._links.self.href", containsString(hostName + requestURI)))
				.andExpect(jsonPath("$._links.first").doesNotExist())
				.andExpect(jsonPath("$._links.next").doesNotExist()).andExpect(jsonPath("$._links.prev").doesNotExist())
				.andExpect(jsonPath("$._links.last").doesNotExist()).andDo(print());
	}

	// đảm bảo _links trả về self, next, last
	@Test
	public void testPaginationLinksInFirstPage() throws Exception {
		int totalElements = 18;
		int pageSize = 5;

		List<Location> listLocations = new ArrayList<>();

		for (int i = 1; i <= pageSize; i++) {
			listLocations
					.add(Location.builder().code("CODE_" + i).cityName("CITY_NAME_" + i).regionName("REGION_NAME_" + i)
							.countryName("COUNTRY_NAME_" + i).countryCode("COUNTRY_CODE_" + i).build());
		}

		int pageNum = 1;
		int totalPages = totalElements / pageSize + 1;
		String sortField = "code";

		Sort sort = Sort.by(sortField);
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);
		Page<Location> page = new PageImpl<>(listLocations, pageable, totalElements);

		Mockito.when(this.locationService.getAllLocationUnTrashedWithFilter(Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyString(), Mockito.anyMap())).thenReturn(page);

		String hostName = "http://localhost";
		String requestURI = END_POINT_PATH + "?page=" + pageNum + "&size=" + pageSize + "&sort=" + sortField;

		String nextPageURI = END_POINT_PATH + "?page=" + (pageNum + 1) + "&size=" + pageSize + "&sort=" + sortField;
		String lastPageURI = END_POINT_PATH + "?page=" + totalPages + "&size=" + pageSize + "&sort=" + sortField;

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
				.andExpect(jsonPath("$._links.self.href", containsString(hostName + requestURI)))
				.andExpect(jsonPath("$._links.first").doesNotExist())
				.andExpect(jsonPath("$._links.next.href", containsString(hostName + nextPageURI)))
				.andExpect(jsonPath("$._links.prev").doesNotExist())
				.andExpect(jsonPath("$._links.last.href", containsString(hostName + lastPageURI))).andDo(print());
	}

	// đảm bảo trả về self, first, prev, next, last
	@Test
	public void testPaginationLinksInMiddlePage() throws Exception {
		int totalElements = 18;
		int pageSize = 5;

		List<Location> listLocations = new ArrayList<>(pageSize);

		for (int i = 1; i <= pageSize; i++) {
			listLocations
					.add(Location.builder().code("CODE_" + i).cityName("CITY_NAME_" + i).regionName("REGION_NAME_" + i)
							.countryName("COUNTRY_NAME_" + i).countryCode("COUNTRY_CODE_" + i).build());
		}

		int pageNum = 3;
		int totalPages = totalElements / pageSize + 1;
		String sortField = "code";

		Sort sort = Sort.by(sortField);
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

		Page<Location> page = new PageImpl<>(listLocations, pageable, totalElements);

		Mockito.when(this.locationService.getAllLocationUnTrashedWithFilter(Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyString(), Mockito.anyMap())).thenReturn(page);

		String hostName = "http://localhost";
		String requestURI = END_POINT_PATH + "?page=" + pageNum + "&size=" + pageSize + "&sort=" + sortField;

		String firstPageURI = END_POINT_PATH + "?page=1&size=" + pageSize + "&sort=" + sortField;
		String nextPageURI = END_POINT_PATH + "?page=" + (pageNum + 1) + "&size=" + pageSize + "&sort=" + sortField;
		String prevPageURI = END_POINT_PATH + "?page=" + (pageNum - 1) + "&size=" + pageSize + "&sort=" + sortField;
		String lastPageURI = END_POINT_PATH + "?page=" + totalPages + "&size=" + pageSize + "&sort=" + sortField;

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
				.andExpect(jsonPath("$._links.first.href", containsString(hostName + firstPageURI)))
				.andExpect(jsonPath("$._links.next.href", containsString(hostName + nextPageURI)))
				.andExpect(jsonPath("$._links.prev.href", containsString(hostName + prevPageURI)))
				.andExpect(jsonPath("$._links.last.href", containsString(hostName + lastPageURI))).andDo(print());
	}

	// đảm bảo trả về self, first, prev
	@Test
	public void testPaginationLinksInLastPage() throws Exception {
		int totalElements = 18;
		int pageSize = 5;

		List<Location> listLocations = new ArrayList<>(pageSize);

		for (int i = 1; i <= pageSize; i++) {
			listLocations
					.add(Location.builder().code("CODE_" + i).cityName("CITY_NAME_" + i).regionName("REGION_NAME_" + i)
							.countryName("COUNTRY_NAME_" + i).countryCode("COUNTRY_CODE_" + i).build());
		}

		int totalPages = (totalElements / pageSize) + 1;
		int pageNum = totalPages;
		String sortField = "code";

		Sort sort = Sort.by(sortField);
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

		Page<Location> page = new PageImpl<>(listLocations, pageable, totalElements);

		Mockito.when(this.locationService.getAllLocationUnTrashedWithFilter(Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyString(), Mockito.anyMap())).thenReturn(page);

		String hostName = "http://localhost";
		String requestURI = END_POINT_PATH + "?page=" + pageNum + "&size=" + pageSize + "&sort=" + sortField;

		String firstPageURI = END_POINT_PATH + "?page=1&size=" + pageSize + "&sort=" + sortField;
		String prevPageURI = END_POINT_PATH + "?page=" + (pageNum - 1) + "&size=" + pageSize + "&sort=" + sortField;

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
				.andExpect(jsonPath("$._links.first.href", containsString(hostName + firstPageURI)))
				.andExpect(jsonPath("$._links.next").doesNotExist())
				.andExpect(jsonPath("$._links.prev.href", containsString(hostName + prevPageURI)))
				.andExpect(jsonPath("$._links.last").doesNotExist()).andDo(print());
	}

	@Test
	public void testGetLocationsByPageShouldReturn400BadRequestBecauseInvalidParams() throws Exception {
		Integer page = 1;
		Integer size = 5;
		String sort = "codesd";

		String requestURI = END_POINT_PATH + "?page=" + page + "&size=" + size + "&sort=" + sort;

		Mockito.when(this.locationService.getAllLocationUnTrashedWithFilter(Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyString(), Mockito.anyMap())).thenReturn(Page.empty());

		mockMvc.perform(get(requestURI)).andExpect(status().isBadRequest()).andDo(print());
	}

	@Test
	public void testGetLocationShouldReturn405MethodNotAllow() throws Exception {
		String code = "A";
		String requestUri = END_POINT_PATH + "/" + code;

		mockMvc.perform(post(requestUri)).andExpect(status().isMethodNotAllowed()).andDo(print());
	}

	@Test
	public void testGetLocationShouldReturn404NotFound() throws Exception {
		String code = "A";
		String requestUri = END_POINT_PATH + "/" + code;

		LocationNotFoundException locationNotFoundException = new LocationNotFoundException(code);

		Mockito.when(this.locationService.getLocationByCode(code)).thenThrow(locationNotFoundException);

		mockMvc.perform(get(requestUri)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errors[0]", is(locationNotFoundException.getMessage()))).andDo(print());
	}

	@Test
	public void testGetLocationShouldReturn200Ok() throws Exception {
		String locationCode = "HCM_VN";
		String requestUri = END_POINT_PATH + "/" + locationCode;

		Location location = new Location();
		location.setCode("HCM_VN");
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Southern Vietnam");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");

		Mockito.when(this.locationService.getLocationByCode(locationCode)).thenReturn(location);

		mockMvc.perform(get(requestUri)).andExpect(status().isOk()).andExpect(status().isOk())
				.andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
				.andExpect(header().string("Cache-Control", containsString("max-age=604800")))
				.andExpect(header().exists("ETag"))
				.andExpect(jsonPath("$._links.self.href", is("http://localhost" + END_POINT_PATH + "/" + locationCode)))
				.andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime/" + locationCode)))
				.andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
				.andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
				.andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
				.andDo(print());
	}

	@Test
	public void testUpdateLocationShouldReturn404NotFound() throws Exception {
		LocationDTO locationDTO = new LocationDTO();
		locationDTO.setCode("HCM_VN");
		locationDTO.setCityName("Ho Chi Minh City");
		locationDTO.setRegionName("Southern Vietnam");
		locationDTO.setCountryName("Vietnam");
		locationDTO.setCountryCode("VN");
		locationDTO.setEnabled(true);

		LocationNotFoundException locationNotFoundException = new LocationNotFoundException(locationDTO.getCode());

		Mockito.when(this.locationService.updateLocation(Mockito.any())).thenThrow(locationNotFoundException);

		String bodyContent = objectMapper.writeValueAsString(locationDTO);

		mockMvc.perform(put(END_POINT_PATH).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errors[0]", is(locationNotFoundException.getMessage()))).andDo(print());
	}

	@Test
	public void testUpdateLocationShouldReturn400BadRequest() throws Exception {
		LocationDTO locationDTO = new LocationDTO();
		locationDTO.setCode("HCM_VN");
		locationDTO.setCityName("Ho Chi Minh City");
		locationDTO.setCountryCode("VN");
		locationDTO.setEnabled(true);

		String bodyContent = objectMapper.writeValueAsString(locationDTO);

		mockMvc.perform(put(END_POINT_PATH).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
				.andExpect(status().isBadRequest()).andDo(print());
	}

	@Test
	public void testUpdateLocationShouldReturn200Ok() throws Exception {
		String locationCode = "HCM_VN";

		Location location = new Location();
		location.setCode(locationCode);
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Southern Vietnam");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");
		location.setEnabled(true);

		LocationDTO locationDTO = new LocationDTO();
		locationDTO.setCode(location.getCode());
		locationDTO.setCityName(location.getCityName());
		locationDTO.setRegionName(location.getRegionName());
		locationDTO.setCountryName(location.getCountryName());
		locationDTO.setCountryCode(location.getCountryCode());

		Mockito.when(this.locationService.updateLocation(location)).thenReturn(location);

		String bodyContent = objectMapper.writeValueAsString(locationDTO);

		mockMvc.perform(put(END_POINT_PATH).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
				.andExpect(status().isOk()).andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
				.andExpect(jsonPath("$._links.self.href", is("http://localhost" + END_POINT_PATH + "/" + locationCode)))
				.andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime/" + locationCode)))
				.andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
				.andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
				.andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void testDeleteLocationShouldReturn404NotFound() throws Exception {
		String code = "A";
		String requestUri = END_POINT_PATH + "/" + code;

		LocationNotFoundException locationNotFoundException = new LocationNotFoundException(code);

		Mockito.doThrow(locationNotFoundException).when(this.locationService).deleteLocation(code);

		mockMvc.perform(delete(requestUri)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errors[0]", is(locationNotFoundException.getMessage()))).andDo(print());
	}

	@Test
	public void testDeleteLocationShouldReturn204NoContent() throws Exception {
		String code = "A";
		String requestUri = END_POINT_PATH + "/" + code;

		Mockito.doNothing().when(this.locationService).deleteLocation(code);

		mockMvc.perform(delete(requestUri)).andExpect(status().isNoContent()).andDo(print());
	}

	@Test
	public void testValidateRequestBodyAllFieldsInvalid() throws Exception {
		Location location = new Location();

		String bodyContent = objectMapper.writeValueAsString(location);

		MvcResult mvcResult = mockMvc
				.perform(post(END_POINT_PATH).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
				.andExpect(status().isBadRequest()).andDo(print()).andReturn();

		String responseBody = mvcResult.getResponse().getContentAsString();

		assertThat(responseBody).contains("Location code cannot be null");
	}

}
