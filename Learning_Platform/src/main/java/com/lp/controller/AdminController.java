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
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.lp.model.Category;
import com.lp.model.Course;
import com.lp.model.Purchase;
import com.lp.model.UserDtls;
import com.lp.model.Video;
import com.lp.service.CategoryService;
import com.lp.service.CourseService;
import com.lp.service.PurchaseService;
import com.lp.service.UserService;
import com.lp.service.VideoService;
import com.lp.util.CommonUtil;
import com.lp.util.PaymentStatus;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CourseService courseService;

	@Autowired
	private UserService userService;

	@Autowired
	private PurchaseService purchaseService;

	@Autowired
	private VideoService videoService;

	@Autowired
	private CommonUtil commonUtil;

	@ModelAttribute
	public void getUserByEmail(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
		}
	}

	@GetMapping("/")
	public String index() {
		return "admin/index";
	}

	@GetMapping("/loadAddCourse")
	public String loadAddProduct(Model m) {
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);
		return "admin/add_Course";
	}

	@GetMapping("/category")
	public String category(Model m) {
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/category";
	}

	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		category.setImagename(imageName);

		Boolean existCategory = categoryService.existCategory(category.getName());
		if (existCategory) {
			session.setAttribute("errorMsg", "Category has empty & name is exist.");
		} else {
			Category saveCategory = categoryService.saveCategory(category);
			if (ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("errorMsg", "Not save ! Internal Server Error");
			} else {
				if (!file.isEmpty()) {
					File saveFile = new ClassPathResource("static/img").getFile();

					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
							+ file.getOriginalFilename());
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				}
				session.setAttribute("succMsg", "Saved successfully");
			}
		}
		return "redirect:/admin/category";
	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		Boolean category = categoryService.deleteCategory(id);
		if (category) {
			session.setAttribute("succMsg", "Category Deleted...");
		} else {
			session.setAttribute("errorMsg", "Something is wrong in server...");
		}
		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		Category oldCategory = categoryService.getCategoryById(category.getId());

		String imageName = file.isEmpty() ? oldCategory.getImagename() : file.getOriginalFilename();

		if (!ObjectUtils.isEmpty(category)) {
			oldCategory.setName(category.getName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImagename(imageName);
		}

		Category updateCategory = categoryService.saveCategory(oldCategory);

		if (!ObjectUtils.isEmpty(updateCategory)) {

			if (!file.isEmpty()) {

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			session.setAttribute("succMsg", "Category Updated...");
		} else {
			session.setAttribute("errorMsg", "Something is wrong in server...");
		}

		return "redirect:/admin/loadEditCategory/" + category.getId();
	}

	@PostMapping("/saveCourse")
	public String saveCourse(@ModelAttribute Course course, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {
		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		course.setImage(imageName);
		Course saveCourse = courseService.saveCourse(course);
		if (!ObjectUtils.isEmpty(saveCourse)) {
			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "course_img" + File.separator
						+ file.getOriginalFilename());
//			System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			session.setAttribute("succMsg", "Course Save Successfully..");
		} else {
			session.setAttribute("errorMsg", "Something is wrong in server...");
		}
		return "redirect:/admin/loadAddCourse";
	}

	@GetMapping("/courses")
	public String loadViewCourse(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "9") Integer pageSize,
			@RequestParam(defaultValue = "") String ch) {
		Page<Course> page = null;
		if (ch != null && ch.length() > 0) {
			page = courseService.searchCoursesPagination(pageNo, pageSize, ch);
		} else {
			page = courseService.getAllCoursesPagination(pageNo, pageSize);
		}
		m.addAttribute("courses", page.getContent());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		return "/admin/courses";
	}

	@GetMapping("/deleteCourse/{id}")
	public String deleteCourse(@PathVariable int id, HttpSession session) {
		Boolean deleteCourse = courseService.deleteCourse(id);
		if (deleteCourse) {
			session.setAttribute("succMsg", "Product Deleted..");
		} else {
			session.setAttribute("errorMsg", "Something is wrong on server..");
		}
		return "redirect:/admin/courses";
	}

	@GetMapping("/loadEditCourse/{id}")
	public String loadEditCourse(@PathVariable int id, Model m) {
		m.addAttribute("course", courseService.getCourseById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/edit_course";
	}

	@PostMapping("/updateCourse")
	public String updateCourse(@ModelAttribute Course course, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {
		Course oldCourse = courseService.getCourseById(course.getId());

		String imageName = file.isEmpty() ? oldCourse.getImage() : file.getOriginalFilename();

		if (!ObjectUtils.isEmpty(course)) {
			oldCourse.setTitle(course.getTitle());
			oldCourse.setDescription(course.getDescription());
			oldCourse.setCategory(course.getCategory());
			oldCourse.setPrice(course.getPrice());
			oldCourse.setStock(course.getStock());
			oldCourse.setAccessDurationInMonths(course.getAccessDurationInMonths());
			oldCourse.setIsActive(course.getIsActive());
			oldCourse.setImage(imageName);
			oldCourse.setDiscount(course.getDiscount());
			int discount = course.getDiscount();
			Double price = course.getPrice();
			oldCourse.setDiscountPrice(price - ((price * discount) / 100));

		}

		if (course.getDiscount() < 0 || course.getDiscount() > 100) {
			session.setAttribute("errorMsg", "Enter valid discount..");
		} else {

			Course updateCourse = courseService.saveCourse(oldCourse);

			if (!ObjectUtils.isEmpty(updateCourse)) {
				if (!file.isEmpty()) {
					File saveFile = new ClassPathResource("static/img").getFile();

					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "course_img" + File.separator
							+ file.getOriginalFilename());

					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				}

				session.setAttribute("succMsg", "Course Updated...");
			} else {
				session.setAttribute("erroeMsg", "Something id wrong in server...");
			}
		}

		return "redirect:/admin/loadEditCourse/" + course.getId();
	}

	@GetMapping("/users")
	public String getAllUser(Model m) {
		List<UserDtls> users = userService.getUsers("ROLE_USER");
		m.addAttribute("users", users);
		return "/admin/users";
	}

	@GetMapping("/updateStatus")
	public String updateUserAccount(@RequestParam Boolean status, @RequestParam Integer id, HttpSession session) {
		Boolean f = userService.getUserByIdOrStatus(id, status);

		if (f) {
			session.setAttribute("succMsg", "Account is updated...");
		} else {
			session.setAttribute("errorMsg", "Something is wrong on server");
		}
		return "redirect:/admin/users";
	}

	@GetMapping("/pending_payments")
	public String viewPendingPayments(Model m) {
		m.addAttribute("pendingPurchases", purchaseService.getAllPayments());
		return "/admin/pending_payments";
	}

	@GetMapping("/verify_payment")
	public String verifyPayment(@RequestParam String purchaseId, HttpSession session) {
		Purchase paymentVerified = purchaseService.verifyPayment(purchaseId);
		try {
			commonUtil.sendMailForPurchase(paymentVerified,
					paymentVerified.getPaymentStatus() + ". Enjoy the course now...");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		if (!ObjectUtils.isEmpty(paymentVerified)) {
			session.setAttribute("succMsg", "Payment is verified...");
		} else if (purchaseService.getPurchaseById(purchaseId).get().getPaymentStatus()
				.equals(PaymentStatus.CANCELED.getName())) {
			session.setAttribute("errorMsg", "Transaction is cancelled by user...");
		} else {
			session.setAttribute("errorMsg", "Something is wrong...");
		}
		return "redirect:/admin/pending_payments";
	}

	@GetMapping("/cancelPaymentStatus")
	public String cancelPaymentStatus(@RequestParam String id, @RequestParam Integer st, HttpSession session) {

		PaymentStatus[] values = PaymentStatus.values();
		String status = null;
		for (PaymentStatus paymentSt : values) {
			if (paymentSt.getId().equals(st)) {
				status = paymentSt.getName();
			}
		}

		Optional<Purchase> purchase = purchaseService.getPurchaseById(id);
		// Add the course to the user's purchased courses
		UserDtls user = purchase.get().getUser();
		user.getCourses().remove(purchase.get().getCourse());

		Purchase updateStatusByAdmin = purchaseService.updateStatusByUser(id, status);

		try {
			commonUtil.sendMailForPurchase(updateStatusByAdmin, status
					+ ". Please try to purchase the course again and provide me with the transactionId once you have completed the transaction.");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateStatusByAdmin)) {
			session.setAttribute("succMsg", "Payment Status Failed...");
		} else {
			session.setAttribute("errorMsg", "Something is wrong...");
		}
		return "redirect:/admin/pending_payments";
	}

	// upload video

	@GetMapping("/courses_video")
	public String loadViewCourseForVideo(Model m) {
		m.addAttribute("courses", courseService.getAllCourse());
		return "/admin/courses_video";
	}

	@GetMapping("/upload_video")
	public String showUploadForm(@RequestParam("courseId") int id, Model m) {
		m.addAttribute("courseId", courseService.getCourseById(id));
		return "/admin/upload_video";
	}

	@PostMapping("/upload-video")
	public String uploadVideo(@RequestParam("courseId") int courseId,

			@RequestParam("title") String title,

			@RequestParam("description") String description,

			@RequestParam("file") MultipartFile file, HttpSession session) {

		// Check if the file is empty
		if (file.isEmpty()) {
			session.setAttribute("errorMsg", "Please select a video to upload.");
			return "redirect:/admin/upload_video?courseId=" + courseId; // Redirect back to the upload page
		}

		// Save video metadata to the database Course courseById =
		Video uploadVideo = new Video();
		uploadVideo.setVideoId(UUID.randomUUID().toString());
		uploadVideo.setCourse(courseService.getCourseById(courseId));
		uploadVideo.setTitle(title);
		uploadVideo.setDescription(description);
		Video saveVideo = videoService.saveVideo(uploadVideo, file);
		if (!ObjectUtils.isEmpty(saveVideo)) {
			session.setAttribute("succMsg", "Video Upload Successfully..");
			return "redirect:/admin/upload_video?courseId=" + courseId;
		} else {
			session.setAttribute("errorMsg", "Something is wrong on server..");
			return null;
		}
	}

	@GetMapping("/add-admin")
	public String loadAddAdmin() {
		return "/admin/add_admin";
	}

	@PostMapping("/saveAdmin")
	public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		Boolean existsEmail = userService.existsEmail(user.getEmail());
		if (existsEmail) {
			session.setAttribute("errorMsg", "Email is already exists...");
		} else {
			String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
			user.setProfileImage(imageName);
			UserDtls saveUser = userService.saveAdmin(user);

			if (!ObjectUtils.isEmpty(saveUser)) {
				if (!file.isEmpty()) {

					File saveFile = new ClassPathResource("static/img").getFile();

					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
							+ file.getOriginalFilename());

					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				}
				session.setAttribute("succMsg", "Admin Saved..");
			} else {
				session.setAttribute("errorMsg", "Something is wrong on server");
			}
		}

		return "redirect:/admin/add-admin";
	}

	@GetMapping("/admins")
	public String getAllAdmin(Model m) {
		List<UserDtls> users = userService.getUsers("ROLE_ADMIN");
		m.addAttribute("admins", users);
		return "/admin/admins";
	}

	@GetMapping("/updateAdminStatus")
	public String updateAdminAccountStatus(@RequestParam Boolean status, @RequestParam Integer id,
			HttpSession session) {
		Boolean f = userService.getUserByIdOrStatus(id, status);

		if (f) {
			session.setAttribute("succMsg", "Account is updated...");
		} else {
			session.setAttribute("errorMsg", "Something is wrong on server");
		}
		return "redirect:/admin/admins";
	}

	@GetMapping("/search-transaction")
	public String searchProduct(@RequestParam String transactionId, Model m, HttpSession session) {
		Purchase purchaseByTransactionId = purchaseService.getPurchaseByTransactionId(transactionId);
		if (transactionId == "") {
			m.addAttribute("pendingPurchases", purchaseService.getAllPayments());
			return "/admin/pending_payments";
		} else if (ObjectUtils.isEmpty(purchaseByTransactionId)) {
			session.setAttribute("errorMsg", "Incorrect Transaction Id");
			return "/admin/pending_payments";
		} else {
			m.addAttribute("purchase", purchaseByTransactionId);
		}

		m.addAttribute("searchPurchase", true);
		return "/admin/pending_payments";

	}

}
