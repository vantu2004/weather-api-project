package com.weatherapi.clientmanager.user.clientapp;

import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weatherapi.clientmanager.ClientAppNotFoundException;
import com.skyapi.weatherforecast.common.AppRole;
import com.skyapi.weatherforecast.common.ClientApp;
import com.skyapi.weatherforecast.common.User;

@Service
@Transactional
public class UserClientAppService {
	static final int APPS_PER_PAGE = 5;
	private UserClientAppRepository repo;
	private PasswordEncoder passwordEncoder;

	public UserClientAppService(UserClientAppRepository repo, PasswordEncoder passwordEncoder) {
		this.repo = repo;
		this.passwordEncoder = passwordEncoder;
	}
	
	public Page<ClientApp> listByPage(Integer userId, int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
			
		PageRequest pageRequest = PageRequest.of(pageNum - 1, APPS_PER_PAGE, sort);
		
		if (keyword != null) {
			return repo.searchByUser(userId, keyword, pageRequest);
		}
		
		return repo.findAllByUser(userId, pageRequest);
	}	
	
	public ClientApp save(User user, ClientApp app) {
		return app.isEditing() ? updateExistingApp(user, app) : saveNewApp(user, app);
	}
	
	private ClientApp updateExistingApp(User user, ClientApp appInForm) {
		ClientApp appInDB = repo.findById(appInForm.getId()).get();
		appInDB.setName(appInForm.getName());
		appInDB.setEnabled(appInForm.isEnabled());	
		
		return repo.save(appInDB);
	}	
	
	private ClientApp saveNewApp(User user, ClientApp app) {
		
		String clientId = RandomStringUtils.randomAlphanumeric(20);
		String clientSecret = RandomStringUtils.randomAlphanumeric(40);
		
		app.setClientId(clientId);
		
		String encodedSecret = passwordEncoder.encode(clientSecret);
		app.setClientSecret(encodedSecret);
		app.setRole(AppRole.READER);
		app.setUser(user);
		
		ClientApp savedApp = repo.save(app);
		savedApp.setRawClientSecret(clientSecret);
		
		return savedApp;
	}
	
	public void updateAppEnabledStatus(User user, Integer appId, boolean enabled) throws ClientAppNotFoundException {
		if (!repo.existsById(appId)) {
			throw new ClientAppNotFoundException("Could not find app with ID " + appId);
		}		
		repo.updateEnabledStatus(user.getId(), appId, enabled);
	}
	
	public void delete(User user, Integer appId) throws ClientAppNotFoundException {
		if (!repo.existsById(appId)) {
			throw new ClientAppNotFoundException("Could not find app with ID " + appId);
		}
		
		repo.trashByUserAndId(user.getId(), appId);
	}
	
	public ClientApp get(User user, Integer appId) throws ClientAppNotFoundException {
		Optional<ClientApp> result = repo.findByUserAndIdNotTrashed(user.getId(), appId);
		if (result.isEmpty()) {
			throw new ClientAppNotFoundException("No app found with the given id: " + appId);
		}
		
		return result.get();
	}	
}
