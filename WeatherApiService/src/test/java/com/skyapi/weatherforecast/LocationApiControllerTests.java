package com.skyapi.weatherforecast;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.location.LocationApiController;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationService;

// chỉ test tầng controller
@WebMvcTest(LocationApiController.class)
public class LocationApiControllerTests {
	private static final String END_POINT_PATH = "/v1/locations";

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

		mockMvc.perform(post(END_POINT_PATH).contentType("application/json").content(bodyContent))
				.andExpect(status().isBadRequest()).andDo(print());
	}

	@Test
	public void testAddShouldReturn201Created() throws Exception {
		Location location = new Location();
		location.setCode("HCM_VN");
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Southern Vietnam");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");

		/*
		 * thay vì dùng logic thật trong service thì dòng này giả lập khi phương thức
		 * add trong service đc gọi với tham số là location "gốc" và nhận về location
		 * "gốc", khi api đc gọi thì location bên api đc chuyển từ json về lại object
		 * nên sẽ khác so vs location "gốc" đc truyền vào dẫn đến phương thức add trong
		 * LocationService nhận vào là location khác, mà theo mong muốn của dòng này là
		 * buộc phải nhận đc location "gốc" nên buộc phải dùng hashCode và equals để so
		 * sánh
		 */
		Mockito.when(this.locationService.add(location)).thenReturn(location);

		String bodyContent = objectMapper.writeValueAsString(location);

		mockMvc.perform(post(END_POINT_PATH).contentType("application/json").content(bodyContent))
				.andExpect(status().isCreated()).andDo(print());
	}

	@Test
	public void testGetLocationsShouldReturn204NoContent() throws Exception {
		/*
		 * Collection là interface mà các List/Set/Map kế thừa, còn Collections chứa các
		 * phương thức thao tác với List/Set/Map
		 */

		/*
		 * giả định khi khi phương thức getAllLocationUnTrashed này đc gọi đến thì trả
		 * về list rỗng
		 */
		Mockito.when(this.locationService.getAllLocationUnTrashed()).thenReturn(Collections.emptyList());

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isNoContent()).andDo(print());
	}

	@Test
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
	public void testGetLocationShouldReturn405MethodNotAllow() throws Exception {
		String code = "A";
		String requestUri = END_POINT_PATH + "/" + code;

		mockMvc.perform(post(requestUri)).andExpect(status().isMethodNotAllowed()).andDo(print());
	}

	@Test
	public void testGetLocationShouldReturn404NotFound() throws Exception {
		String code = "A";
		String requestUri = END_POINT_PATH + "/" + code;

		mockMvc.perform(get(requestUri)).andExpect(status().isNotFound()).andDo(print());
	}

	@Test
	public void testGetLocationShouldReturn200Ok() throws Exception {
		String code = "'HCM_VN'";
		String requestUri = END_POINT_PATH + "/" + code;

		Location location = new Location();
		location.setCode("HCM_VN");
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Southern Vietnam");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");

		Mockito.when(this.locationService.getLocationByCode(code)).thenReturn(location);

		mockMvc.perform(get(requestUri)).andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void testUpdateLocationShouldReturn404NotFound() throws Exception {
		Location location = new Location();
		location.setCode("HCM_VN");
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Southern Vietnam");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");
		location.setEnabled(true);

		Mockito.when(this.locationService.updateLocation(location))
				.thenThrow(new LocationNotFoundException("Location Not Found!"));

		String bodyContent = objectMapper.writeValueAsString(location);

		mockMvc.perform(put(END_POINT_PATH).contentType("application/json").content(bodyContent))
				.andExpect(status().isNotFound()).andDo(print());
	}

	@Test
	public void testUpdateLocationShouldReturn400BadRequest() throws Exception {
		Location location = new Location();
		location.setCode("HCM_VN");
		location.setCityName("Ho Chi Minh City");
		location.setCountryCode("VN");
		location.setEnabled(true);

		Mockito.when(this.locationService.updateLocation(location)).thenReturn(location);

		String bodyContent = objectMapper.writeValueAsString(location);

		mockMvc.perform(put(END_POINT_PATH).contentType("application/json").content(bodyContent))
				.andExpect(status().isBadRequest()).andDo(print());
	}

	@Test
	public void testUpdateLocationShouldReturn200Ok() throws Exception {
		Location location = new Location();
		location.setCode("HCM_VN");
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Southern Vietnam");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");
		location.setEnabled(true);

		Mockito.when(this.locationService.updateLocation(location)).thenReturn(location);

		String bodyContent = objectMapper.writeValueAsString(location);

		mockMvc.perform(put(END_POINT_PATH).contentType("application/json").content(bodyContent))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void testDeleteLocationShouldReturn404NotFound() throws Exception {
		String code = "A";
		String requestUri = END_POINT_PATH + "/" + code;

		Mockito.doThrow(LocationNotFoundException.class).when(this.locationService).deleteLocation(code);

		mockMvc.perform(delete(requestUri)).andExpect(status().isNotFound()).andDo(print());
	}

	@Test
	public void testDeleteLocationShouldReturn204NoContent() throws Exception {
		String code = "A";
		String requestUri = END_POINT_PATH + "/" + code;

		Mockito.doNothing().when(this.locationService).deleteLocation(code);

		mockMvc.perform(delete(requestUri)).andExpect(status().isNoContent()).andDo(print());
	}
}
