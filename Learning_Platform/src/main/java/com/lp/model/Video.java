package com.lp.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
public class Video {

	@Id
	private String videoId;
	
	private String title;
	
	private String description;
	
	private String contentType;
	
	private String filePath;
	
	@ManyToOne
	private Course course;
}
