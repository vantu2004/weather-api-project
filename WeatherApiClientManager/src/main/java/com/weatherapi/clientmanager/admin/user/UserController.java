package com.weatherapi.clientmanager.admin.user;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.skyapi.weatherforecast.common.User;
import com.skyapi.weatherforecast.common.UserType;

@Controller
@RequestMapping("/users")
public class UserController {
	private String defaultRedirectURL = "redirect:/users";
	private UserService service;

	@Autowired
	public UserController(UserService service) {
		this.service = service;
	}
	
	@GetMapping
	public String listFirstPage(Model model) {
		return listByPage(model, 1, "name", "asc", null);
	}
	
	@GetMapping("/page/{pageNum}")
	public String listByPage(Model model, @PathVariable(name = "pageNum") int pageNum,
			String sortField, String sortDir, String keyword) {
		Page<User> page = service.listByPage(pageNum, sortField, sortDir, keyword);
		List<User> listUsers = page.getContent();
		
		long startCount = (pageNum - 1) * UserService.USERS_PER_PAGE + 1;
		long endCount = startCount + UserService.USERS_PER_PAGE - 1;
		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		
		model.addAttribute("listUsers", listUsers);
	    model.addAttribute("currentPage", pageNum);
	    model.addAttribute("totalPages", page.getTotalPages());
	    model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("sortField", sortField);	
		model.addAttribute("sortDir", sortDir);	
		model.addAttribute("keyword", keyword);
		model.addAttribute("moduleURL", "/users");
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		return "admin/users/user_list";
	}
	
	@GetMapping("/new")
	public String newUserForm(Model model, HttpServletRequest request) {
		String userType = request.getParameter("type");
		User newUser = new User();
		
		if ("client".equals(userType)) {
			newUser.setType(UserType.CLIENT);
		} else {
			newUser.setType(UserType.ADMIN);
		}
		
		model.addAttribute("pageTitle", "Add User");
		model.addAttribute("user", newUser);
		model.addAttribute("isEdit", false);
		
		return "admin/users/user_form";
	}
	
	@PostMapping("/save")
	public String saveUser(User user, RedirectAttributes ra, Model model) {
		boolean isAddNew = (user.getId() == null);

		try {
			service.save(user);
			
			String message = "";
			if (isAddNew) {
				message = "The user " + user.getName() + " has been added successfully.";
			} else {
				message = "The user ID " + user.getId() + " (" + user.getName() + ") has been updated successfully.";
			}
			
			ra.addFlashAttribute("message", message);
			
			return "redirect:/users";
		} catch (UserEmailNotUniqueException ex) {
			if (isAddNew) {
				model.addAttribute("pageTitle", "Add User");
			} else {			
				model.addAttribute("pageTitle", "Edit User (ID: " + user.getId() + ")");
			}
			
			model.addAttribute("message", ex.getMessage());
			model.addAttribute("user", user);
			model.addAttribute("isEdit", !isAddNew);	
			
			return "admin/users/user_form";
		}
	}
	
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable("id") Integer id, Model model) {
		try {
			User user = service.get(id);
			model.addAttribute("pageTitle", "Edit User (ID: " + id + ")");
			model.addAttribute("user", user);
			model.addAttribute("isEdit", true);
			
			return "admin/users/user_form";
		} catch (UserNotFoundException ex) {
			return "redirect:/users";			
		}
	}
	
	@GetMapping("/delete/{id}")
	public String deleteUser(@PathVariable("id") Integer id, RedirectAttributes ra) {
		String message = "";
		
		try {
			service.delete(id);
			message = "The user ID " + id + " has been deleted successfully.";
		} catch (UserNotFoundException ex) {
			message = ex.getMessage();
		}	
		
		ra.addFlashAttribute("message", message);
		return "redirect:/users";			
	}
	
	@GetMapping("/{id}/enabled/{status}")
	public String updateUserEnabledStatus(@PathVariable("id") Integer id,
			@PathVariable("status") boolean enabled, RedirectAttributes redirectAttributes) {
		
		String message = "";
		
		try {
			service.updateUserEnabledStatus(id, enabled);
			String status = enabled ? "enabled" : "disabled";
			message = "The user ID " + id + " has been " + status;
		} catch (UserNotFoundException ex) {
			message = ex.getMessage();
		}		
		
		redirectAttributes.addFlashAttribute("message", message);
		
		return defaultRedirectURL;
	}	
}
