package com.weatherapi.clientmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.weatherapi.clientmanager.admin.user.UserEmailNotUniqueException;
import com.weatherapi.clientmanager.admin.user.UserService;
import com.weatherapi.clientmanager.security.CustomUserDetails;
import com.skyapi.weatherforecast.common.User;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MainController {

	private UserService service;

	@Autowired
	public MainController(UserService service) {
		this.service = service;
	}
	
	@GetMapping({"", "/"})
	public String viewHomePage(HttpServletRequest request, Model model) {
		model.addAttribute("request", request);
		return "index";
	}
	
	@GetMapping("/signin")
	public String viewLoginPage() {
		return "signin";
	}	
	
	@GetMapping("/signup")
	public String showSignUpForm(Model model) {
		model.addAttribute("user", new User());
		return "signup";
	}
	
	@PostMapping("/signup")
	public String addClientUser(User user, Model model) {
		try {
			service.addClientUser(user);
		} catch (UserEmailNotUniqueException ex) {
			
			model.addAttribute("message", ex.getMessage());
			model.addAttribute("user", user);
			
			return "signup";
		}		
		return "index";
	}
	
	@GetMapping("/account")
	public String showAccountForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
		model.addAttribute("userDetails", userDetails.getUser());
		return "account_form";
	}
	
	@PostMapping("/account")
	public String updateAccountDetails(@AuthenticationPrincipal CustomUserDetails userDetails, 
			String name, String password, Model model) {
		
		service.updateAccount(userDetails.getUser(), name, password);
		model.addAttribute("message", "Your account details have been updated");
		return "message";
	}
}
