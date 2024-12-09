package com.lp.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.lp.util.AppConstant;
import com.lp.model.UserDtls;
import com.lp.repositoy.UserRepository;
import com.lp.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDtls saveUser(UserDtls user) {
		user.setRole("ROLE_USER");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		String encode = passwordEncoder.encode(user.getPassword());
		user.setPassword(encode);
		UserDtls userDtls = userRepository.save(user);
		return userDtls;
	}

	@Override
	public UserDtls getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public List<UserDtls> getUsers(String role) { 
		return userRepository.findByRole(role);
	}

	@Override
	public Boolean getUserByIdOrStatus(Integer id, Boolean status) {
		Optional<UserDtls> userById = userRepository.findById(id);
		if (userById.isPresent()) {
			UserDtls userDtls = userById.get();
			userDtls.setIsEnable(status);
			userRepository.save(userDtls);
			return true;
		}

		return false;
	}

	@Override
	public void increaseFailedAttempt(UserDtls user) {
		int failedAttempt = user.getFailedAttempt() + 1;
		user.setFailedAttempt(failedAttempt);
		userRepository.save(user);
	}

	@Override
	public void userAccountLock(UserDtls user) {
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepository.save(user);
	}

	@Override
	public Boolean unlockAccountTimeExpired(UserDtls user) {
		long time = user.getLockTime().getTime();
		long unLockTime = time + AppConstant.UNLOCK_DURATION_TIME;

		long currentTime = System.currentTimeMillis();

		if (unLockTime < currentTime) {
			user.setAccountNonLocked(true);
			user.setFailedAttempt(0);
			user.setLockTime(null);
			userRepository.save(user);
			return true;
		}
		return false;
	}

	@Override
	public void resetAttempt(int userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public UserDtls updateUser(UserDtls userByToken) {
		return userRepository.save(userByToken);
		
	}

	@Override
	public void updateUserByResetToken(String Email, String reset_token) {
		UserDtls byEmail = userRepository.findByEmail(Email);
		byEmail.setResetToken(reset_token);
		userRepository.save(byEmail);
	}

	@Override
	public UserDtls getUserByToken(String token) {
		return userRepository.findByResetToken(token);
	}

	@Override
	public UserDtls saveAdmin(UserDtls user) {
		user.setRole("ROLE_ADMIN");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		String encode = passwordEncoder.encode(user.getPassword());
		user.setPassword(encode);
		UserDtls userDtls = userRepository.save(user);
		return userDtls;
	}
	
	@Override
	public UserDtls updateUserProfile(UserDtls user, MultipartFile img) {

		UserDtls dbUser = userRepository.findById(user.getId()).get();

		if (!img.isEmpty()) {
			dbUser.setProfileImage(img.getOriginalFilename());
		}

		if (!ObjectUtils.isEmpty(dbUser)) {

			dbUser.setName(user.getName());
			dbUser.setMobileNumber(user.getMobileNumber());
			dbUser = userRepository.save(dbUser);
		}

		try {
			if (!img.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ img.getOriginalFilename());

//			System.out.println(path);
				Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dbUser;
	}

	@Override
	public UserDtls getUserById(Integer id) {
		Optional<UserDtls> byId = userRepository.findById(id);
		if (byId.isPresent()) {
			return byId.get();
		}
		return null;
	}

	@Override
	public Boolean existsEmail(String email) {
		return userRepository.existsByEmail(email);
	}

}
