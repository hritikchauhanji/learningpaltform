package com.lp.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Course {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(length = 500)
	private String title;
	
	@Column(length = 5000)
	private String description;
	
	@OneToMany(mappedBy = "course")
	private List<Video> videos;
	
	private int accessDurationInMonths;
	
	@ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    private Set<UserDtls> users = new HashSet<>();
	
	private String category;
	
	private Double price;
	
	private int stock;
	
	private String image;
	
	private int discount;
	
	private Double discountPrice;
	
	private Boolean isActive;
}
