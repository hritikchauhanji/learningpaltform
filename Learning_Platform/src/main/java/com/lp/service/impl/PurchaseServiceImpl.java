package com.lp.service.impl;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lp.model.Purchase;
import com.lp.model.UserDtls;
import com.lp.repositoy.PurchaseRepository;
import com.lp.repositoy.UserRepository;
import com.lp.service.PurchaseService;
import com.lp.service.UserService;
import com.lp.util.CommonUtil;
import com.lp.util.PaymentStatus;

import jakarta.mail.MessagingException;

@Service
public class PurchaseServiceImpl implements PurchaseService {
	
	@Autowired
	private PurchaseRepository purchaseRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CommonUtil commonUtil;

	@Override
	public Purchase savePurchase(Purchase purchase) {
		purchase.setId(UUID.randomUUID().toString());
		Purchase purchaseSave = purchaseRepository.save(purchase);
		try {
			commonUtil.sendMailForPurchase(purchaseSave, " is pending for verification. We will notify you once confirmed.");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		return purchaseSave;
	}


	@Override
	public boolean hasAccessToCourse(int userId, int courseId) {
	    List<Purchase> purchases = purchaseRepository.findByUserIdAndCourseId(userId, courseId);
	    
	    if (purchases != null && !purchases.isEmpty()) {
	        // Assuming we want to take the latest purchase (based on the purchase date)
	        Purchase latestPurchase = purchases.get(0); // or loop to find the most recent purchase
	        
	        // Check if the course access is still valid
	        return LocalDate.now().isBefore(latestPurchase.getAccessExpirationDate());
	    }
	    return false;
	}



	@Override
	public Purchase verifyPayment(String purchaseId) {
		 Purchase purchase = purchaseRepository.findById(purchaseId).orElse(null);
	        if (purchase != null && (purchase.getPaymentStatus().equals(PaymentStatus.IN_PENDING.getName()) ||  purchase.getPaymentStatus().equals(PaymentStatus.FAILED.getName()))) {
	            purchase.setPaymentStatus(PaymentStatus.IN_COMPLETE.getName());
	            
	            // Set purchase date
	            LocalDate purchaseDate = LocalDate.now();
	            purchase.setPurchaseDate(purchaseDate);

	            // Get course's access duration and calculate expiration date
	            int accessDurationInMonths = purchase.getCourse().getAccessDurationInMonths();
	            LocalDate expirationDate = purchaseDate.plus(accessDurationInMonths, ChronoUnit.MONTHS);
	            purchase.setAccessExpirationDate(expirationDate);

	            Purchase purchaseSave = purchaseRepository.save(purchase);

	            // Add the course to the user's purchased courses
	            UserDtls user = purchase.getUser();
	            user.getCourses().add(purchase.getCourse());

	            userRepository.save(user);
	            return purchaseSave;
	        }
	        return null;
	}


	@Override
	public List<Purchase> getPendingPayments() {
		return purchaseRepository.findByPaymentStatus(PaymentStatus.IN_PENDING.getName());
	}

	@Override
	public Optional<Purchase> getPurchaseById(String id) {
		return purchaseRepository.findById(id);
	}


	@Override
	public List<Purchase> getPurchaseByUserId(int userId) {
		return purchaseRepository.findByUserId(userId);
	}


	@Override
	public Purchase updateStatusByUser(String id, String status) {
		Optional<Purchase> findById = purchaseRepository.findById(id);
		if (findById.isPresent()) {
			Purchase purchase = findById.get();
			purchase.setPaymentStatus(status);
			Purchase purchaseSave = purchaseRepository.save(purchase);
			return purchaseSave;
		}
		return null;
	}


	@Override
	public List<Purchase> getAllPayments() {
		return purchaseRepository.findAll();
	}


	@Override
	public Purchase getPurchaseByTransactionId(String transactionId) {
		return purchaseRepository.findByTransactionId(transactionId);
	}



}
