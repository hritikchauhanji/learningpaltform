package com.lp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lp.model.UserDtls;
import com.lp.repositoy.UserRepository;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDtls user = userRepository.findByEmail(username);
		
		if(user==null) {
			throw new UsernameNotFoundException("Username not found");
		}
		return new CustomUser(user);
	}

}
