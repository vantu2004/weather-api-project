package com.weatherapi.clientmanager.admin.clientapp;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weatherapi.clientmanager.ClientAppNotFoundException;
import com.weatherapi.clientmanager.admin.location.LocationNotFoundException;
import com.weatherapi.clientmanager.admin.location.LocationService;
import com.weatherapi.clientmanager.admin.user.UserNotFoundException;
import com.skyapi.weatherforecast.common.AppRole;
import com.skyapi.weatherforecast.common.ClientApp;
import com.skyapi.weatherforecast.common.Location;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/apps")
public class AdminClientAppController {
	private String defaultRedirectURL = "redirect:/apps";
	private AdminClientAppService appService;
	private LocationService locationService;

	public AdminClientAppController(AdminClientAppService appService, LocationService locationService) {
		this.appService = appService;
		this.locationService = locationService;
	}

	@GetMapping
	public String listFirstPage(Model model) {
		return listByPage(model, 1, "name", "asc", null);
	}
	
	@GetMapping("/page/{pageNum}")
	public String listByPage(Model model, @PathVariable(name = "pageNum") int pageNum,
			String sortField, String sortDir, String keyword) {
		Page<ClientApp> page = appService.listByPage(pageNum, sortField, sortDir, keyword);
		List<ClientApp> listApps = page.getContent();
		
		long startCount = (pageNum - 1) * AdminClientAppService.APPS_PER_PAGE + 1;
		long endCount = startCount + AdminClientAppService.APPS_PER_PAGE - 1;
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
		model.addAttribute("moduleURL", "/apps");
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		return "admin/client_apps/app_list";
	}
	
	@GetMapping("/{id}/enabled/{status}")
	public String updateAppEnabledStatus(@PathVariable("id") Integer id,
			@PathVariable("status") boolean enabled, RedirectAttributes redirectAttributes) {
		
		String message = "";
		
		try {
			appService.updateAppEnabledStatus(id, enabled);
			String status = enabled ? "enabled" : "disabled";
			message = "The app ID " + id + " has been " + status;
		} catch (ClientAppNotFoundException ex) {
			message = ex.getMessage();
		}		
		
		redirectAttributes.addFlashAttribute("message", message);
		
		return defaultRedirectURL;
	}	
	
	@GetMapping("/new")
	public String showNewForm(Model model, HttpServletRequest request) {
		String role = request.getParameter("role");
		ClientApp newApp = new ClientApp();
		
		AppRole appRole = AppRole.valueOf(role.toUpperCase());
		newApp.setRole(appRole);
		
		model.addAttribute("pageTitle", "Create New App");
		model.addAttribute("isEdit", false);
		model.addAttribute("app", newApp);
		
		return "admin/client_apps/app_form";
	}
	
	@PostMapping("/save")
	public String saveApp(ClientApp clientApp, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		try {
			boolean isEditing = clientApp.isEditing();
			
			String userEmail = getUserEmail(request);
			System.out.println("userEmail = " + userEmail);
			
			if (clientApp.getRole().equals(AppRole.UPDATER)) {
				String locationCode = getLocationCode(request);
				System.out.println("locationCode = " + locationCode);
				
				Location location = locationService.get(locationCode);
				clientApp.setLocation(location);
			}
			
			ClientApp updatedApp = appService.save(userEmail, clientApp);
			
			model.addAttribute("pageTitle", getPageTitle(clientApp));
			model.addAttribute("app", updatedApp);
			
			if (isEditing) {
				String message = "The app ID " + clientApp.getId() + " has been updated.";
				redirectAttributes.addFlashAttribute("message", message);
				return "redirect:/apps";
			} else {
				return "admin/client_apps/created_app_form";
			}
		} catch (InvalidUserFieldException | UserNotFoundException | InvalidLocationFieldException | LocationNotFoundException ex) {
			
			model.addAttribute("pageTitle", getPageTitle(clientApp));
			model.addAttribute("app", clientApp);
			model.addAttribute("message", ex.getMessage());
			
			return "admin/client_apps/app_form";
		}
	}
	
	private String getPageTitle(ClientApp app) {
		return app.isEditing() ? "Create New App" : "Edit App";
	}
	
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			ClientApp clientApp = appService.get(id);
			model.addAttribute("pageTitle", "Edit App (ID: " + id + ")");
			model.addAttribute("app", clientApp);
			model.addAttribute("isEdit", true);
			
			return "admin/client_apps/app_form";
		} catch (ClientAppNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
			return defaultRedirectURL;
		}
	}	
	
	@GetMapping("/detail/{id}")
	public String showDetailForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			ClientApp clientApp = appService.get(id);
			model.addAttribute("pageTitle", "Details of App ID: " + id);
			model.addAttribute("app", clientApp);
			
			return "admin/client_apps/app_detail";
		} catch (ClientAppNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
			return defaultRedirectURL;
		}
	}
	
	@GetMapping("/delete/{id}")
	public String deleteApp(@PathVariable("id") Integer id, RedirectAttributes ra) {
		String message = "";
		
		try {
			appService.delete(id);
			message = "The app ID " + id + " has been deleted successfully.";
		} catch (ClientAppNotFoundException ex) {
			message = ex.getMessage();
		}	
		
		ra.addFlashAttribute("message", message);
		return "redirect:/apps";			
	}	
	
	private String getUserEmail(HttpServletRequest request) throws InvalidUserFieldException {
		String user = request.getParameter("assignedUser");
		String[] array = user.split(" - ");
		if (array.length != 2) {
			throw new InvalidUserFieldException("The value for field User must be in the form of <Name> - <email>, e.g. John Doe - john.doe@gmail.com");
		}
		
		return array[1];
	}
	
	private String getLocationCode(HttpServletRequest request) throws InvalidLocationFieldException {
		String user = request.getParameter("assignedLocation");
		String[] array = user.split(" - ");
		if (array.length != 2) {
			throw new InvalidLocationFieldException("The value for field User must be in the form of <Code> - <city name, region name, country name>, e.g. SF_USA - San Franciso, Califorina, United States");
		}
		
		return array[0];
	}	
}
