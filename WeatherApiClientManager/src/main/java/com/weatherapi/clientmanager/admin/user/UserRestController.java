package com.weatherapi.clientmanager.admin.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skyapi.weatherforecast.common.User;

@RestController
public class UserRestController {

	private UserService service;

	@Autowired
	public UserRestController(UserService service) {
		this.service = service;
	}	
	
	@PostMapping("/users/search")
	public ResponseEntity<?> search(String keyword) {
		System.out.println("User search keyword: " + keyword);
		
		List<User> result = service.searchAutoComplete(keyword);
		
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		
		return ResponseEntity.ok(result);
	}
}
