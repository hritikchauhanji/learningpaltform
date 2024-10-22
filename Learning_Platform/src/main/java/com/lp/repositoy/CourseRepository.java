package com.lp.repositoy;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lp.model.Course;

public interface CourseRepository extends JpaRepository<Course, Integer> {

	List<Course> findByIsActiveTrue();
	
	List<Course> findByCategory(String category);
	
	List<Course> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2);

	Page<Course> findByIsActiveTrue(Pageable pageable);

	Page<Course> findByCategory(Pageable pageable, String category);

	Page<Course> findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2,
			Pageable pageable);

	Page<Course> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2,
			Pageable pageable);

}
