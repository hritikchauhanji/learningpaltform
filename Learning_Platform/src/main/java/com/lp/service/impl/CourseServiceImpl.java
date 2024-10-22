package com.lp.service.impl;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.lp.model.Course;
import com.lp.repositoy.CourseRepository;
import com.lp.service.CourseService;

@Service
public class CourseServiceImpl implements CourseService {
	
	@Autowired
	private CourseRepository courseRepository;

	@Override
	public Course saveCourse(Course course) {
		return courseRepository.save(course);
		
	}

	@Override
	public List<Course> getAllCourse() {
		return courseRepository.findAll();
	}
	
	@Override
	public Page<Course> getAllCoursesPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return courseRepository.findAll(pageable);
	}


	@Override
	public Boolean deleteCourse(int id) {
		Course course = courseRepository.findById(id).orElse(null);
		if (!ObjectUtils.isEmpty(course)) {
			courseRepository.deleteById(id);
			return true;
		}
		return false;
	}

	@Override
	public Course getCourseById(int id) {
		 Course course = courseRepository.findById(id).orElse(null);
		 return course;
	}

	@Override
	public List<Course> getAllActiveCourse(String category) {
		List<Course> courses;
		if(ObjectUtils.isEmpty(category)) {
			courses = courseRepository.findByIsActiveTrue();
		} else {
			courses = courseRepository.findByCategory(category);
		}
		return courses;
	}

	
	
	

	@Override
	public Page<Course> getAllActiveCoursePagination(Integer pageNo, Integer pageSize, String category) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Course> pageCourse = null;

		if (ObjectUtils.isEmpty(category)) {
			pageCourse = courseRepository.findByIsActiveTrue(pageable);
		} else {
			pageCourse = courseRepository.findByCategory(pageable, category);
		}
		return pageCourse;
	}

	@Override
	public Page<Course> searchActiveCoursePagination(Integer pageNo, Integer pageSize, String category, String ch) {
		Page<Course> pageCourse = null;
		Pageable pageable = PageRequest.of(pageNo, pageSize);

		pageCourse = courseRepository.findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch,
				ch, pageable);

//		if (ObjectUtils.isEmpty(category)) {
//			pageProduct = productRepository.findByIsActiveTrue(pageable);
//		} else {
//			pageProduct = productRepository.findByCategory(pageable, category);
//		}
		return pageCourse;
	}

	@Override
	public Page<Course> searchCoursesPagination(Integer pageNo, Integer pageSize, String ch) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return courseRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
	}

	/*
	 * @Override public List<Course> searchCourse(String ch) { return
	 * courseRepository.
	 * findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch); }
	 */
	

	

}
