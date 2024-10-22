package com.lp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
@Entity
public class Purchase {
    @Id
    private String id;

    @ManyToOne
    private UserDtls user;

    @ManyToOne
    private Course course;
    
    private Double amount;

    private String paymentStatus; // e.g., "PENDING", "COMPLETED", "FAILED"
    
    private LocalDate purchaseDate;
    
    private LocalDate accessExpirationDate;

	/* private String upiId; */
    
    private String transactionId; // UPI transaction reference
    
    private boolean acceptTerms;

    // Getters and setters...
}
