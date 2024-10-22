package com.lp.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

import lombok.Data;

@Data
@Entity
public class UserDtls {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	private String mobileNumber;

	@Column(unique = true)
	private String email;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinTable(name = "user_courses", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "course_id"))
	private Set<Course> courses = new HashSet<>();

	private String password;

	private String profileImage;

	private String role;

	private Boolean isEnable;

	private Boolean accountNonLocked;

	private Integer failedAttempt;

	private Date lockTime;
	
	private String resetToken;
}
