package com.lp.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.google.zxing.WriterException;
import com.lp.model.Course;
import com.lp.model.Purchase;
import com.lp.model.UserDtls;
import com.lp.service.CourseService;
import com.lp.service.PurchaseService;
import com.lp.service.UserService;
import com.lp.service.impl.QRCodeService;
import com.lp.util.PaymentStatus;

import jakarta.servlet.http.HttpSession;

@Controller
public class PaymentController {

	@Autowired
	private UserService userService;

	@Autowired
	private CourseService courseService;

	@Autowired
	private QRCodeService qrCodeService;

	@Autowired
	private PurchaseService purchaseService;

	@ModelAttribute
	public void getUserByEmail(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
		}
	}

	@GetMapping("/purchase/{id}")
	public String view_course_payment(@PathVariable int id, Model m, Principal p) throws WriterException, IOException {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			Course course = courseService.getCourseById(id);
			String payeeAddress = "6396629193@ybl";
			String transactionId = "txn_" + id;
			double amount = course.getDiscountPrice();
			String transactionNote = "Payment for " + course.getTitle();

			// Generate UPI link
			String upiLink = String.format("upi://pay?pa=%s&pn=LearningPlatform&tr=%s&am=%.2f&cu=INR&tn=%s",
					payeeAddress, transactionId, amount, transactionNote);

			// Generate QR Code
			byte[] qrCodeImage = qrCodeService.generateQRCode(upiLink);

			// Encode QR Code to Base64
			String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeImage);

			// Pass the Base64-encoded image to the model
			m.addAttribute("user", userDtls);
			m.addAttribute("qrCodeBase64", qrCodeBase64);
			m.addAttribute("course", course);
			return "payment_page";
		}
		return "login";
	}

	@PostMapping("/confirm-payment")
	public String confirmPayment(@RequestParam int courseId, @RequestParam String transactionId,
			@RequestParam(value = "termsCheck", required = false) String termsCheck, Principal p, Model m,
			HttpSession session) {
		if (termsCheck == null) {
			session.setAttribute("errorMsg", "You must agree to the terms and conditions.");
			return "redirect:/purchase/" + courseId;
		} else {
			// Fetch user and course details
			if (p != null) {
				UserDtls user = userService.getUserByEmail(p.getName());
				Course course = courseService.getCourseById(courseId);

				if (course == null) {
					m.addAttribute("message", "Course not found.");
					return "errorMsg";
				}

				// Create a purchase record with PENDING status
				Purchase purchase = new Purchase();
				purchase.setUser(user);
				purchase.setCourse(course);
				purchase.setAmount(course.getDiscountPrice());
				purchase.setPaymentStatus(PaymentStatus.IN_PENDING.getName());
				purchase.setPurchaseDate(LocalDate.now());
				purchase.setAccessExpirationDate(LocalDate.now());
				purchase.setTransactionId(transactionId);
				purchase.setAcceptTerms(true);
				purchaseService.savePurchase(purchase);

				// Notify admin for manual verification (could be via email or admin dashboard)
				// For simplicity, redirect to a thank you page
				m.addAttribute("message", "Your payment is pending verification. We will notify you once confirmed.");
				return "payment_pending";
			}
		}
		return "login";
	}
}
