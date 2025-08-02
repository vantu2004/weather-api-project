package com.weatherapi.clientmanager.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;

import com.skyapi.weatherforecast.common.User;
import com.skyapi.weatherforecast.common.UserType;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class UserRepositoryTests {

	@Autowired private UserRepository repo;
	
	@Test
	public void testAddAdminUser() {
		User user = new User();
		user.setName("Nam Ha Minh");
		user.setEmail("nam@skyapi.com");
		user.setType(UserType.ADMIN);

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		user.setPassword(passwordEncoder.encode("nam2024"));
		
		User savedUser = repo.save(user);
		assertThat(savedUser).isNotNull();
	}
	
	@Test
	public void testAddClientUser() {
		User user = new User();
		user.setName("Mike Murray");
		user.setEmail("mike.murray@gmail.com");
		user.setType(UserType.CLIENT);
		user.setPassword("mike123");
		
		User savedUser = repo.save(user);
		assertThat(savedUser).isNotNull();
	}	
	
	@Test
	public void testEnableUser() {
		Integer userId = 3;
		repo.updateEnabledStatus(userId, true);
		
		User user = repo.findById(userId).get();
		assertThat(user.isEnabled()).isTrue();
	}
	
	@Test
	public void testDisableUser() {
		Integer userId = 3;
		repo.updateEnabledStatus(userId, false);
		
		User user = repo.findById(userId).get();
		assertThat(user.isEnabled()).isFalse();
	}
	
	@Test
	public void testTrashUser() {
		Integer userId = 18;
		repo.trashById(userId);
		
		User user = repo.findById(userId).get();
		assertThat(user.isTrashed()).isTrue();
	}
	
	@Test
	public void testSearchDisabledUser() {
		String keyword = "Messi";
		List<User> result = repo.search(keyword);
		assertThat(result).isEmpty();
	}
	
	@Test
	public void testSearchTrashedUser() {
		String keyword = "tim.webb@gmail.com";
		List<User> result = repo.search(keyword);
		assertThat(result).isEmpty();
	}
	
	@Test
	public void testSearchUsersFoundByName() {
		String keyword = "Alex";
		List<User> result = repo.search(keyword);
		assertThat(result).isNotEmpty();
		
		result.forEach(System.out::println);
	}
	
	@Test
	public void testSearchUsersFoundByEmail() {
		String keyword = "gmail.com";
		List<User> result = repo.search(keyword);
		assertThat(result).isNotEmpty();
		
		result.forEach(System.out::println);
	}	
}
