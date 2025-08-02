package com.weatherapi.clientmanager.user.clientapp;

import java.security.Principal;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weatherapi.clientmanager.ClientAppNotFoundException;
import com.weatherapi.clientmanager.security.CustomUserDetails;
import com.skyapi.weatherforecast.common.ClientApp;
import com.skyapi.weatherforecast.common.User;

@Controller
@RequestMapping("/myapps")
public class UserClientAppController {
	
	private UserClientAppService service;

	public UserClientAppController(UserClientAppService service) {
		this.service = service;
	}
	
	@GetMapping
	public String listFirstPage(Model model, HttpServletRequest request) {
		return listByPage(model, 1, "name", "asc", null, request);
	}
	
	@GetMapping("/page/{pageNum}")
	public String listByPage(Model model, @PathVariable(name = "pageNum") int pageNum,
			String sortField, String sortDir, String keyword, HttpServletRequest request) {
		
		User user = getAuthenticatedUser(request);
		
		Page<ClientApp> page = service.listByPage(user.getId(), pageNum, sortField, sortDir, keyword);
		
		List<ClientApp> listApps = page.getContent();
		
		long startCount = (pageNum - 1) * UserClientAppService.APPS_PER_PAGE + 1;
		long endCount = startCount + UserClientAppService.APPS_PER_PAGE - 1;
		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		
		model.addAttribute("listApps", listApps);
	    model.addAttribute("currentPage", pageNum);
	    model.addAttribute("totalPages", page.getTotalPages());
	    model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("sortField", sortField);	
		model.addAttribute("sortDir", sortDir);	
		model.addAttribute("keyword", keyword);
		model.addAttribute("moduleURL", "/myapps");
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		return "user/user_app_list";
	}		
	
	User getAuthenticatedUser(HttpServletRequest request) {
		Principal principal = request.getUserPrincipal();
		if (principal instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
			CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();
			return userDetails.getUser();
		}
		
		return null;
	}
	
	@GetMapping("/new")
	public String showNewAppForm(Model model) {
		model.addAttribute("pageTitle", "Create New App");
		model.addAttribute("app", new ClientApp());
		
		return "user/user_app_form";
	}
	
	@PostMapping("/save")
	public String saveApp(ClientApp app, Model model, HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		User user = getAuthenticatedUser(request);
		boolean isEditing = app.isEditing();
		
		ClientApp createdApp = service.save(user, app);
		
		if (isEditing) {
			
			String message = "The app ID " + app.getId() + " has been updated.";
			redirectAttributes.addFlashAttribute("message", message);
			
			return "redirect:/myapps";
			
		} else {
			model.addAttribute("app", createdApp);
			model.addAttribute("pageTitle", "App Created (ID: " + createdApp.getId() + ")");
		
			return "user/user_created_app_form";
		}
	}
	
	@GetMapping("/detail/{id}")
	public String viewAppDetail(@PathVariable("id") Integer appId, Model model, HttpServletRequest request,
			RedirectAttributes ra) {
		User user = getAuthenticatedUser(request);
		
		try {
			ClientApp clientApp = service.get(user, appId);
			model.addAttribute("app", clientApp);
			model.addAttribute("pageTitle", "App Details (ID: " + clientApp.getId() + ")");
			
			return "user/user_app_detail";
		} catch (ClientAppNotFoundException ex) {
			ra.addFlashAttribute("message", ex.getMessage());
			return "redirect:/myapps";	
		}				
	}
	
	@GetMapping("/{id}/enabled/{status}")
	public String updateAppEnabledStatus(@PathVariable("id") Integer appId, HttpServletRequest request,
			@PathVariable("status") boolean enabled, RedirectAttributes redirectAttributes) {
		
		String message = "";
		User user = getAuthenticatedUser(request);
		
		try {
			service.updateAppEnabledStatus(user, appId, enabled);
			String status = enabled ? "enabled" : "disabled";
			message = "The app ID " + appId + " has been " + status;
		} catch (ClientAppNotFoundException ex) {
			message = ex.getMessage();
		}		
		
		redirectAttributes.addFlashAttribute("message", message);
		
		return "redirect:/myapps";
	}
	
	@GetMapping("/delete/{id}")
	public String deleteApp(@PathVariable("id") Integer appId, HttpServletRequest request, RedirectAttributes ra) {
		
		String message = "";
		User user = getAuthenticatedUser(request);
		
		try {
			service.delete(user, appId);
			message = "The app ID " + appId + " has been deleted successfully.";
		} catch (ClientAppNotFoundException ex) {
			message = ex.getMessage();
		}	
		
		ra.addFlashAttribute("message", message);
		return "redirect:/myapps";			
	}
	
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable("id") Integer appId, Model model, HttpServletRequest request, 
			RedirectAttributes redirectAttributes) {
		
		User user = getAuthenticatedUser(request);
		
		try {
			ClientApp clientApp = service.get(user, appId);
			model.addAttribute("pageTitle", "Edit App (ID: " + appId + ")");
			model.addAttribute("app", clientApp);
			model.addAttribute("isEdit", true);
			
			return "user/user_app_form";
		} catch (ClientAppNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
			return "redirect:/myapps";
		}
	}	
}
