package com.lp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.lp.config.AuthFailedHandlerImpl;

@Configuration
public class SecurityConfig {
	
	@Autowired
	private AuthenticationSuccessHandler authenticationSuccessHandler;
	
	
	@Autowired
	@Lazy
	private AuthFailedHandlerImpl authFailedHandlerImpl;

    @Bean
    PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
    
    @Bean
    UserDetailsService userDetailService() {
    	return new UserDetailServiceImpl();
    }
    
    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider() {
    	DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    	authenticationProvider.setPasswordEncoder(passwordEncoder());
    	authenticationProvider.setUserDetailsService(userDetailService());
    	return authenticationProvider;
    }
    
    @Bean 
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	http.csrf(csrf->csrf.disable()).cors(cors->cors.disable())
    	.authorizeHttpRequests(req->req.requestMatchers("/user/**").hasRole("USER")
    			.requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/**").permitAll())
    	.formLogin(form->form.loginPage("/signin").loginProcessingUrl("/login")
//    			.defaultSuccessUrl("/")
    			.failureHandler(authFailedHandlerImpl)
    			.successHandler(authenticationSuccessHandler))
    	.logout(logout->logout.permitAll());
    	return http.build();
    }
}
