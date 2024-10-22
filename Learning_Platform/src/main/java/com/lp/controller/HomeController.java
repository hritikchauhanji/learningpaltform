package com.lp.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.lp.model.Category;
import com.lp.model.Course;
import com.lp.model.UserDtls;
import com.lp.service.CategoryService;
import com.lp.service.CourseService;
import com.lp.service.UserService;
import com.lp.service.impl.QRCodeService;
import com.lp.util.CommonUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CourseService courseService;

	@Autowired
	private UserService userService;

	@Autowired
	private QRCodeService qrCodeService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@ModelAttribute
	public void getUserByEmail(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
		}
	}

	@GetMapping("/")
	public String index(Model m, @RequestParam(value = "category", defaultValue = "") String category) {
		m.addAttribute("categories", categoryService.getAllActiveCategories());
		m.addAttribute("courses", courseService.getAllActiveCourse(category));
		m.addAttribute("paramValue", category);
		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@GetMapping("/courses")
	public String courses(Model m, @RequestParam(value = "category", defaultValue = "") String category,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "4") Integer pageSize,
			@RequestParam(defaultValue = "") String ch) {
		m.addAttribute("categories", categoryService.getAllActiveCategories());
		/* m.addAttribute("courses", courseService.getAllActiveCourse(category)); */
		m.addAttribute("paramValue", category);

		Page<Course> page = null;
		if (ch == "") {
			page = courseService.getAllActiveCoursePagination(pageNo, pageSize, category);
		} else {
			page = courseService.searchActiveCoursePagination(pageNo, pageSize, category, ch);
		}

		List<Course> courses = page.getContent();
		m.addAttribute("courses", courses);
		m.addAttribute("coursesSize", courses.size());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		return "courses";
	}

	@GetMapping("/view_course/{id}")
	public String view_course(@PathVariable int id, Model m) {
		m.addAttribute("course", courseService.getCourseById(id));
		return "view_course";
	}

	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam("file") MultipartFile file, HttpSession session)
			throws IOException {

		Boolean existsEmail = userService.existsEmail(user.getEmail());
		if (existsEmail) {
			session.setAttribute("errorMsg", "Email is already exists...");
		} else {
			String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
			user.setProfileImage(imageName);
			UserDtls saveUser = userService.saveUser(user);

			if (!ObjectUtils.isEmpty(saveUser)) {
				if (!file.isEmpty()) {

					File saveFile = new ClassPathResource("static/img").getFile();

					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
							+ file.getOriginalFilename());

					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				}
				session.setAttribute("succMsg", "Register Saved..");
			} else {
				session.setAttribute("errorMsg", "Something is wrong on server");
			}
		}

		return "redirect:/register";
	}

	@GetMapping("/forgot-password")
	public String forgotPassword() {
		return "forgot_password";
	}

	@PostMapping("/forgot-password")
	public String showForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
			throws UnsupportedEncodingException, MessagingException {
		UserDtls userByEmail = userService.getUserByEmail(email);
		if (ObjectUtils.isEmpty(userByEmail)) {
			session.setAttribute("errorMsg", "Invalid email...");
		} else {
			String reset_token = UUID.randomUUID().toString();
			userService.updateUserByResetToken(email, reset_token);

			// Generate URL :
			String url = commonUtil.generateUrl(request) + "/reset-password?token=" + reset_token;

			Boolean sendMail = commonUtil.sendMail(url, email);
			if (sendMail) {
				session.setAttribute("succMsg", "Check your email...");
			} else {
				session.setAttribute("errorMsg", "Something is wrong on server...");
			}
		}
		return "redirect:/forgot-password";
	}

	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, Model m) {

		UserDtls userByToken = userService.getUserByToken(token);

		if (userByToken == null) {
			m.addAttribute("msg", "Your link is invalid or expired !!");
			return "message";
		}
		m.addAttribute("token", token);
		return "reset_password";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token, @RequestParam String password, Model m) {

		UserDtls userByToken = userService.getUserByToken(token);
		if (userByToken == null) {
			m.addAttribute("errorMsg", "Your link is invalid or expired !!");
			return "message";
		} else {
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			// session.setAttribute("succMsg", "Password change successfully");
			m.addAttribute("msg", "Password change successfully");

			return "message";
		}

	}

	/*
	 * @GetMapping("/search") public String searchProduct(@RequestParam String ch,
	 * Model m) { List<Course> searchProducts = courseService.searchCourse(ch);
	 * m.addAttribute("courses", searchProducts); List<Category> categories =
	 * categoryService.getAllActiveCategories(); m.addAttribute("categories",
	 * categories); return "courses";
	 * 
	 * }
	 */
	
	@GetMapping("/termscondition")
	public String termsCondition() {
		return "termscondition";
	}

}
