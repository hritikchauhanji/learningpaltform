package com.lp.repositoy;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lp.model.Video;

public interface VideoRepository extends JpaRepository<Video, String> {

	Optional<Video> findByTitle(String title);

	List<Video> findByCourseId(int courseId);
}
