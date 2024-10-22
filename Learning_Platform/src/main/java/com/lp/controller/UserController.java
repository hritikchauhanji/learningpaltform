package com.lp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.lp.model.Purchase;
import com.lp.model.UserDtls;
import com.lp.model.Video;
import com.lp.service.PurchaseService;
import com.lp.service.UserService;
import com.lp.service.VideoService;
import com.lp.util.AppConstant;
import com.lp.util.CommonUtil;
import com.lp.util.PaymentStatus;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private PurchaseService purchaseService;

	@Autowired
	private VideoService videoService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@ModelAttribute
	public void getUserByEmail(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
		}
	}

	@GetMapping("/")
	public String home() {
		return "user/home";
	}

	@GetMapping("/viewCourseVideos/courseId={courseId}/videos")
	public String viewCourseVideos(@PathVariable int courseId, Principal p, Model m) {
		// Check if the user has access to the course
		Integer userId = p != null ? getUserIdByPrincipal(p) : null;
		if (userId != null && purchaseService.hasAccessToCourse(userId, courseId)) {
			List<Video> videos = videoService.getVideosByCourseId(courseId);
			m.addAttribute("courseId", courseId);
			m.addAttribute("videos", videos);
			return "/user/course_videos";
		} else {
			return "redirect:/";
		}
	}

	private int getUserIdByPrincipal(Principal p) {
		String email = p.getName(); // This retrieves the logged-in user's email/username from Principal
		UserDtls user = userService.getUserByEmail(email); // Fetch the user from the database using the email
		return user != null ? user.getId() : null; // Return the user ID if the user exists, else return null
	}

	@GetMapping("/loadVideo/courseId={courseId}/videoId={videoId}")
	public String streamvideo(@PathVariable("videoId") String videoId, @PathVariable("courseId") int courseId,
			Principal p, Model m) {
		// Check if the user has access to the course
		Integer userId = p != null ? getUserIdByPrincipal(p) : null;
		Video videoById = videoService.getVideoById(videoId);
		if (userId != null && purchaseService.hasAccessToCourse(userId, courseId) && courseId == videoById.getCourse().getId()) {
			m.addAttribute("video", videoService.getVideoById(videoId));
			return "/user/play";
		} else {
			return "redirect:/courses";
		}
	}

	/*
	 * @GetMapping("/stream/{videoId}") public ResponseEntity<Resource>
	 * stream(@PathVariable("videoId") String videoId) {
	 * 
	 * Video video = videoService.getVideoById(videoId); String contentType =
	 * video.getContentType(); String filePath = video.getFilePath(); Resource
	 * resource = new FileSystemResource(filePath); if (contentType == null) {
	 * contentType = "application/octet-stream"; }
	 * 
	 * 
	 * return
	 * ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(
	 * resource);
	 * 
	 * 
	 * }
	 */

	@SuppressWarnings("resource")
	// stream video in chunks
	@GetMapping("/stream/range/{videoId}")
	public ResponseEntity<Resource> streamVideoRange(@PathVariable String videoId,
			@RequestHeader(value = "Range", required = false) String range) {

		// System.out.println(range);

		Video video = videoService.getVideoById(videoId);

		Path path = Paths.get(video.getFilePath());

		Resource resource = new FileSystemResource(path);

		String contentType = video.getContentType();

		if (contentType == null) {
			contentType = "application/octet-stream";

		}

		// file ki length
		long fileLength = path.toFile().length();

		// pahle jaisa hi code hai kyuki range header null
		if (range == null) {
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
		}

		// calculating start and end range

		long rangeStart;

		long rangeEnd;

		String[] ranges = range.replace("bytes=", "").split("-");
		rangeStart = Long.parseLong(ranges[0]);

		rangeEnd = rangeStart + AppConstant.CHUNK_SIZE - 1;

		if (rangeEnd >= fileLength) {
			rangeEnd = fileLength - 1;
		}

		/*
		 * if (ranges.length > 1) { rangeEnd = Long.parseLong(ranges[1]); } else {
		 * rangeEnd = fileLength - 1; }
		 * 
		 * if (rangeEnd > fileLength - 1) { rangeEnd = fileLength - 1; }
		 */
//		System.out.println("range start : " + rangeStart);
//		System.out.println("range end : " + rangeEnd);
		InputStream inputStream;

		try {

			inputStream = Files.newInputStream(path);
			inputStream.skip(rangeStart);
			long contentLength = rangeEnd - rangeStart + 1;

			byte[] data = new byte[(int) contentLength];
			int read = inputStream.read(data, 0, data.length);
//			System.out.println("read(number of bytes) : " + read);

			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			headers.add("X-Content-Type-Options", "nosniff");
			headers.setContentLength(contentLength);

			return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(headers)
					.contentType(MediaType.parseMediaType(contentType)).body(new ByteArrayResource(data));

		} catch (IOException ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}

	
	  //serve hls playlist
	  
	  //master.m2u8 file
	  
	  @Value("${file.video.hsl}") 
	  private String HSL_DIR;
	  
	  @GetMapping("/{videoId}/master.m3u8")
	  public ResponseEntity<Resource>
	  serverMasterFile(
	  
	  @PathVariable String videoId ) {
	  
	   //creating path 
	   Path path = Paths.get(HSL_DIR, videoId, "master.m3u8");
	  
	  System.out.println(path);
	  
	  if (!Files.exists(path)) { return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	  }
	  
	  Resource resource = new FileSystemResource(path);
	  
	  return ResponseEntity .ok() .header( HttpHeaders.CONTENT_TYPE,
	  "application/vnd.apple.mpegurl" ) .body(resource);
	  
	  
	  }
	  
	  //serve the segments
	  
	  @GetMapping("/{videoId}/{segment}.ts")
	  public ResponseEntity<Resource>
	  serveSegments(
	  
	  @PathVariable String videoId,
	  
	  @PathVariable String segment ) {
	  
	  // create path for segment 
		  Path path = Paths.get(HSL_DIR, videoId, segment +".ts"); 
	  if (!Files.exists(path)) { return new
	  ResponseEntity<>(HttpStatus.NOT_FOUND); }
	  
	  Resource resource = new FileSystemResource(path);
	  
	  return ResponseEntity .ok() .header( HttpHeaders.CONTENT_TYPE, "video/mp2t" )
	  .body(resource);
	  
	  }
	  
	  @GetMapping("/payment_status")
		public String payment_status(Model m,Principal p) {
		  Integer userId = p != null ? getUserIdByPrincipal(p) : null;
			m.addAttribute("paymentStatus", purchaseService.getPurchaseByUserId(userId));
			return "/user/payment_status";
		}
	  
	  @GetMapping("/updatePaymentStatus")
		public String updatePaymentStatus(@RequestParam String id, @RequestParam Integer st, HttpSession session) {
		  	PaymentStatus[] values = PaymentStatus.values();
		  	String status = null;
		  	for(PaymentStatus paymentSt:values) {
		  		if (paymentSt.getId().equals(st)) {
		  			status = paymentSt.getName();
		  		}
		  	}
		  	
		  	Purchase updateStatusByUser = purchaseService.updateStatusByUser(id, status);
		  	try {
				commonUtil.sendMailForPurchase(updateStatusByUser, status);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	
		  	if (!ObjectUtils.isEmpty(updateStatusByUser)) {
		  		session.setAttribute("succMsg", "Payment Status Updated...");
			} else {
				session.setAttribute("errorMsg", "Something is wrong...");
			}
			return "redirect:/user/payment_status";
		}
	  
	  @GetMapping("/profile")
	  public String profile() {
		  return "/user/profile";
	  }
	 
	  @PostMapping("/update-profile")
		public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
			UserDtls updateUserProfile = userService.updateUserProfile(user, img);
			if (ObjectUtils.isEmpty(updateUserProfile)) {
				session.setAttribute("errorMsg", "Profile not updated");
			} else {
				session.setAttribute("succMsg", "Profile Updated");
			}
			return "redirect:/user/profile";
		}
	  
	  @PostMapping("/change-password")
		public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
				HttpSession session) {
			UserDtls userById = userService.getUserById(getUserIdByPrincipal(p));
			 

			boolean matches = passwordEncoder.matches(currentPassword, userById.getPassword());

			if (matches) {
				String encodePassword = passwordEncoder.encode(newPassword);
				userById.setPassword(encodePassword);
				UserDtls updateUser = userService.updateUser(userById);
				if (ObjectUtils.isEmpty(updateUser)) {
					session.setAttribute("errorMsg", "Password not updated !! Error in server");
				} else {
					session.setAttribute("succMsg", "Password Updated sucessfully");
				}
			} else {
				session.setAttribute("errorMsg", "Current Password incorrect");
			}

			return "redirect:/user/profile";
		}
}
