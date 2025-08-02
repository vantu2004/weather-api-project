package com.weatherapi.clientmanager.admin.user;

import java.util.List;
import java.util.NoSuchElementException;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.skyapi.weatherforecast.common.User;
import com.skyapi.weatherforecast.common.UserType;

@Service
@Transactional
public class UserService {
	static final int USERS_PER_PAGE = 5;
	private UserRepository repo;

	@Autowired private PasswordEncoder passwordEncoder;
	
	@Autowired
	public UserService(UserRepository repo) {
		this.repo = repo;
	}
	
	public Page<User> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
			
		PageRequest pageRequest = PageRequest.of(pageNum - 1, USERS_PER_PAGE, sort);
		
		if (keyword != null) {
			return repo.findAll(keyword, pageRequest);
		}
		
		return repo.findAll(pageRequest);
	}
	
	public void save(User userInForm) throws UserEmailNotUniqueException {
		if (!isEmailUnique(userInForm)) {
			throw new UserEmailNotUniqueException("The email " + userInForm.getEmail() + " is already taken");
		}
		
		boolean isEdit = (userInForm.getId() != null);
		
		if (isEdit && userInForm.getPassword().isEmpty()) {
			User userInDB = repo.findById(userInForm.getId()).get();
			userInForm.setPassword(userInDB.getPassword());
		} else {
			encodePassword(userInForm);
		}
		
		repo.save(userInForm);
	}
	
	private boolean isEmailUnique(User userInForm) {
		User userByEmail = repo.findByEmail(userInForm.getEmail());
		if (userByEmail != null && !userByEmail.getId().equals(userInForm.getId())) {
			return false;
		}
		
		return true;
	}
	
	public void addClientUser(User user) throws UserEmailNotUniqueException {
		if (!isEmailUnique(user)) {
			throw new UserEmailNotUniqueException("The email " + user.getEmail() + " is already taken");
		}
		
		user.setType(UserType.CLIENT);
		user.setEnabled(false);
		encodePassword(user);
		
		repo.save(user);
	}
	
	void encodePassword(User user) {
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
	}
	
	public User get(Integer id) throws UserNotFoundException {
		try {
			return repo.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new UserNotFoundException("Could not find user with ID " + id);
		}
	}
	
	public void delete(Integer id) throws UserNotFoundException {
		if (!repo.existsById(id)) {
			throw new UserNotFoundException("Could not find user with ID " + id);
		}
		
		repo.trashById(id);
	}
	
	public void updateUserEnabledStatus(Integer id, boolean enabled) throws UserNotFoundException {
		if (!repo.existsById(id)) {
			throw new UserNotFoundException("Could not find user with ID " + id);
		}		
		repo.updateEnabledStatus(id, enabled);
	}	
	
	public List<User> searchAutoComplete(String keyword) {
		return repo.search(keyword);
	}
	
	public void updateAccount(User user, String name, String newPassword) {
		user.setName(name);
		
		if (!newPassword.isEmpty()) {
			user.setPassword(newPassword);
			encodePassword(user);
		}
		
		repo.save(user);
	}
}
