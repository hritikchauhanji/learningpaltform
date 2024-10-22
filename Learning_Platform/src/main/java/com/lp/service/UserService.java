package com.lp.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.lp.model.UserDtls;

public interface UserService {

	public UserDtls saveUser(UserDtls user);

	public UserDtls getUserByEmail(String email);

	public List<UserDtls> getUsers(String role);

	public Boolean getUserByIdOrStatus(Integer id, Boolean status);

	public void increaseFailedAttempt(UserDtls user);

	public void userAccountLock(UserDtls user);

	public Boolean unlockAccountTimeExpired(UserDtls user);

	public void resetAttempt(int userId);

	public void updateUserByResetToken(String Email, String reset_token);

	public UserDtls getUserByToken(String token);

	public UserDtls updateUser(UserDtls userByToken);
	
	public UserDtls saveAdmin(UserDtls user);

	UserDtls updateUserProfile(UserDtls user, MultipartFile img);
	
	UserDtls getUserById(Integer id);
	
	Boolean existsEmail(String email);
}
