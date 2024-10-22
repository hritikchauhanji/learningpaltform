package com.lp.util;

import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.lp.model.Purchase;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

	@Autowired
	private JavaMailSender mailSender;

	public Boolean sendMail(String url, String reciepentEmail) throws UnsupportedEncodingException, MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("hritikchji@gmail.com", "Learning Platform");
		helper.setTo(reciepentEmail);

		String content = "<p>Hello,</p>" + "<p>You have requested to reset your password.</p>"
				+ "<p>Click the link below to change your password:</p>" + "<p><a href=\"" + url
				+ "\">Change my password</a></p>";
		helper.setSubject("Password Reset");
		helper.setText(content, true);
		mailSender.send(message);
		return true;

	}

	public String generateUrl(HttpServletRequest request) {
		/*
		 * String getUrl =request.getRequestURI().toString(); return
		 * getUrl.replace(request.getServletPath(), "");
		 */

		String scheme = request.getScheme(); // http or https
		String serverName = request.getServerName(); // localhost or domain name
		int serverPort = request.getServerPort(); // port number (8080, 80, 443, etc.)
		String contextPath = request.getContextPath(); // application context path ("/" if root)

		// Build the full URL, including the scheme, server name, port, and context path
		String url = scheme + "://" + serverName;

		// Include the port if it's not the default port (80 for HTTP, 443 for HTTPS)
		if ((serverPort != 80 && "http".equals(scheme)) || (serverPort != 443 && "https".equals(scheme))) {
			url += ":" + serverPort;
		}

		// Add the context path
		url += contextPath;

		return url;
	}
	
	String msg = null;
	
	public Boolean sendMailForPurchase(Purchase purchase, String status) throws UnsupportedEncodingException, MessagingException {
		msg="<p>Hello [[name]],</p>"
				+ "<p>Thank you for join us. <b>Payment Status is [[orderStatus]]</b>.</p>"
				+ "<p><b>Course Details:</b></p>"
				+ "<p>Name : [[CourseName]]</p>"
				+ "<p>Category : [[category]]</p>"
				+ "<p>Price : [[price]]</p>"
				+ "<p>Payment Type : Qr Code or upi id</p>";
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("daspabitra55@gmail.com", "Shooping Cart");
		helper.setTo(purchase.getUser().getEmail());

		msg=msg.replace("[[name]]",purchase.getUser().getName());
		msg=msg.replace("[[orderStatus]]",status);
		msg=msg.replace("[[CourseName]]",purchase.getCourse().getTitle());
		msg=msg.replace("[[category]]", purchase.getCourse().getCategory());
		msg=msg.replace("[[price]]", purchase.getCourse().getDiscountPrice().toString());
		
		helper.setSubject("Course Purchase Status");
		helper.setText(msg, true);
		mailSender.send(message);
		return true;
	}

}
