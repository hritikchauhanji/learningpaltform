package com.lp.service;

import java.util.List;
import java.util.Optional;

import com.lp.model.Purchase;

public interface PurchaseService {

	Purchase savePurchase(Purchase purchase);
	
	List<Purchase> getPendingPayments();
	
	List<Purchase> getAllPayments();
	
	Optional<Purchase> getPurchaseById(String id);
	
	boolean hasAccessToCourse(int userId, int courseId);
	
    Purchase verifyPayment(String purchaseId);

	List<Purchase> getPurchaseByUserId(int userId);
	
	Purchase updateStatusByUser(String id, String status);
	
	Purchase getPurchaseByTransactionId(String transactionId);
}
