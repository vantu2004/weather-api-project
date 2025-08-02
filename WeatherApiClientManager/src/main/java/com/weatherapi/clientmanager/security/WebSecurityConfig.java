package com.weatherapi.clientmanager.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {
	
    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
        		auth -> auth.requestMatchers("/signin", "/signup").permitAll()
        		.requestMatchers("/users/**", "/apps/**").hasAuthority("ADMIN")
        		.requestMatchers("/myapps/**").hasAuthority("CLIENT")
        		.anyRequest().authenticated()
               )
                .formLogin(formLogin -> formLogin
                		.loginPage("/signin")
                		.usernameParameter("email")
                		.defaultSuccessUrl("/", true)
                		.permitAll()
                )
                .rememberMe(withDefaults())
                .logout(logout -> logout.logoutUrl("/signout").permitAll());
 
 
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/images/**", "/js/**", "/webjars/**");
    }
}
