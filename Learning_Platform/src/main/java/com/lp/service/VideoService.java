package com.lp.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.lp.model.Video;

public interface VideoService {

    //save  video
    Video saveVideo(Video video, MultipartFile file);


    // get video by  id
    Video getVideoById(String videoId);
    
    //get video by courseId
    
    List<Video> getVideosByCourseId(int courseId);


    // get video by title

    Video getVideoByTitle(String title);

    List<Video> getAllVideo();


    //video processing
    String processVideo(String videoId);

}
