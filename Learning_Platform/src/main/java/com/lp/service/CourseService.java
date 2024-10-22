package com.lp.service;


import java.util.List;

import org.springframework.data.domain.Page;

import com.lp.model.Course;

public interface CourseService {

	public Course saveCourse(Course course);
	
	public List<Course> getAllCourse();
	
	public Boolean deleteCourse(int id);
	
	public Course getCourseById(int id);
	
	public List<Course> getAllActiveCourse(String category);

	/* public List<Course> searchCourse(String ch); */

	public Page<Course> getAllActiveCoursePagination(Integer pageNo, Integer pageSize, String category);

	public Page<Course> searchActiveCoursePagination(Integer pageNo, Integer pageSize, String category, String ch);

	Page<Course> getAllCoursesPagination(Integer pageNo, Integer pageSize);

	Page<Course> searchCoursesPagination(Integer pageNo, Integer pageSize, String ch);
	
	
}
