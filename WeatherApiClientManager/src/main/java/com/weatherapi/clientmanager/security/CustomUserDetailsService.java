package com.weatherapi.clientmanager.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.weatherapi.clientmanager.admin.user.UserRepository;
import com.skyapi.weatherforecast.common.User;

public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepo.findByEmail(username);
		
		if (user != null) {
			return new CustomUserDetails(user);
		}
		
		throw new UsernameNotFoundException("Could not find user with email: " + username);
	}

}
