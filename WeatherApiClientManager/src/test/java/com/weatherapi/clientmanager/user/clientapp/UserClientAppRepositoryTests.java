package com.weatherapi.clientmanager.user.clientapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.skyapi.weatherforecast.common.ClientApp;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class UserClientAppRepositoryTests {

	@Autowired private UserClientAppRepository repo;
	
	@Test
	public void testFindByUserAndIdNotFound() {
		Integer appId = 1;
		Integer userId = 1;
		
		ClientApp clientApp = repo.findByUserAndId(userId, appId);
		
		assertThat(clientApp).isNull();
	}
	
	@Test
	public void testFindByUserAndIdFound() {
		Integer appId = 1;
		Integer userId = 21;
		
		ClientApp clientApp = repo.findByUserAndId(userId, appId);
		
		assertThat(clientApp).isNotNull();
	}	
	
	@Test
	public void testEnableAppSuccess() {
		Integer appId = 2;
		Integer userId = 3;
		
		repo.updateEnabledStatus(userId, appId, true);
		
		ClientApp app = repo.findById(appId).get();
		assertThat(app.isEnabled()).isTrue();
	}
	
	@Test
	public void testDisableAppSuccess() {
		Integer appId = 9;
		Integer userId = 3;
		
		repo.updateEnabledStatus(userId, appId, false);
		
		ClientApp app = repo.findById(appId).get();
		assertThat(app.isEnabled()).isFalse();
	}
	
	@Test
	public void testTrashAppSuccess() {
		Integer appId = 1;
		Integer userId = 21;
		
		repo.trashByUserAndId(userId, appId);
		
		ClientApp app = repo.findById(appId).get();
		assertThat(app.isTrashed()).isTrue();
	}
		
}
