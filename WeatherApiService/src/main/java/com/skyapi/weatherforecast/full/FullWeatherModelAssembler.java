package com.skyapi.weatherforecast.full;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class FullWeatherModelAssembler
		implements RepresentationModelAssembler<FullWeatherDTO, EntityModel<FullWeatherDTO>> {

	@Override
	public EntityModel<FullWeatherDTO> toModel(FullWeatherDTO fullWeatherDTO) {
		EntityModel<FullWeatherDTO> entityModel = EntityModel.of(fullWeatherDTO);
		entityModel.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByIPAddress(null)).withSelfRel());

		return entityModel;
	}

	public EntityModel<FullWeatherDTO> addLinksByLocation(String locationCode, FullWeatherDTO fullWeatherDTO) {
		return EntityModel.of(fullWeatherDTO)
				.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByLocationCode(locationCode))
						.withSelfRel());
	}

}
