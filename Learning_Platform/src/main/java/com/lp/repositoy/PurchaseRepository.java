package com.lp.repositoy;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lp.model.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, String> {

	 List<Purchase> findByPaymentStatus(String status);

//	Purchase findByUserIdAndCourseId(long userId, long courseId);

//	Purchase findUniqueByUserIdAndCourseId(int userId, int courseId);

	List<Purchase> findByUserIdAndCourseId(int userId, int courseId);
	
	List<Purchase> findByUserId(int userId);
	
	Purchase findByTransactionId(String transationId);
}
