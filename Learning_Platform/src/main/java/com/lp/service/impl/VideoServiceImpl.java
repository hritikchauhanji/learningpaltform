package com.lp.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.lp.model.Video;
import com.lp.repositoy.VideoRepository;
import com.lp.service.VideoService;

import jakarta.annotation.PostConstruct;

@Service
public class VideoServiceImpl implements VideoService {

	@Value("${files.video}")
	String DIR;

	@Value("${file.video.hsl}")
	String HSL_DIR;

	@Autowired
	private VideoRepository videoRepository;

	@PostConstruct
	public void init() {
		File file = new File(DIR);

		try {
			Files.createDirectories(Paths.get(HSL_DIR));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (!file.exists()) {
			file.mkdir();
//			System.out.println("Folder Created:");
		} else {
//			System.out.println("Folder already created:");
		}
	}

	@Override
	public Video saveVideo(Video video, MultipartFile file) {

		// original file name

		try {
			String originalFilename = file.getOriginalFilename();
			String contentType = file.getContentType();
			InputStream inputStream = file.getInputStream();

			// folder path: create

			String cleanFileName = StringUtils.cleanPath(originalFilename);
			String cleanFolder = StringUtils.cleanPath(DIR);

			// create path
			Path path = Paths.get(cleanFolder, cleanFileName);

			// copy file to the folder
			Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

			// video meta data
			video.setContentType(contentType);
			video.setFilePath(path.toString());

			Video savedVideo = videoRepository.save(video);
			// processing video
//			processVideo(savedVideo.getVideoId());

			// delete actual video file and database entry if exception

			// metadata save
			return savedVideo;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Error in processing video ");
		}
	}

	@Override
	public Video getVideoById(String videoId) {

		Video video = videoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("video not found"));
		return video;
	}

	@Override
	public Video getVideoByTitle(String title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Video> getAllVideo() {
		return videoRepository.findAll();
	}

	@Override
	public String processVideo(String videoId) {
		Video video = this.getVideoById(videoId);
		String filePath = video.getFilePath();

		// path where to store data:
		Path videoPath = Paths.get(filePath);

//	        String output360p = HSL_DIR + videoId + "/360p/";
//	        String output720p = HSL_DIR + videoId + "/720p/";
//	        String output1080p = HSL_DIR + videoId + "/1080p/";

		try {
//	            Files.createDirectories(Paths.get(output360p));
//	            Files.createDirectories(Paths.get(output720p));
//	            Files.createDirectories(Paths.get(output1080p));

			// ffmpeg command
			Path outputPath = Paths.get(HSL_DIR, videoId);

			Files.createDirectories(outputPath);


			String ffmpegCmd = String.format(
				    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s\\segment_%%03d.ts\" \"%s\\master.m3u8\"",
				    videoPath.toString(), 
				    outputPath.toString(), 
				    outputPath.toString()
				);

//	            StringBuilder ffmpegCmd = new StringBuilder();
//	            ffmpegCmd.append("ffmpeg  -i ")
//	                    .append(videoPath.toString())
//	                    .append(" -c:v libx264 -c:a aac")
//	                    .append(" ")
//	                    .append("-map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k ")
//	                    .append("-map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k ")
//	                    .append("-map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k ")
//	                    .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
//	                    .append("-master_pl_name ").append(HSL_DIR).append(videoId).append("/master.m3u8 ")
//	                    .append("-f hls -hls_time 10 -hls_list_size 0 ")
//	                    .append("-hls_segment_filename \"").append(HSL_DIR).append(videoId).append("/v%v/fileSequence%d.ts\" ")
//	                    .append("\"").append(HSL_DIR).append(videoId).append("/v%v/prog_index.m3u8\"");

			System.out.println(ffmpegCmd);
			// file this command
			// On Windows
//			ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
//
//			processBuilder.inheritIO();
//			Process process = processBuilder.start();
			
			 // Run the FFmpeg command using ProcessBuilder
	        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
	        processBuilder.redirectErrorStream(true); // Capture error stream
	        Process process = processBuilder.start();

	        // Capture and print the output/error from FFmpeg
	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line;
	        while ((line = reader.readLine()) != null) {
	            System.out.println(line);  // Print FFmpeg output or error to the console
	        }
			int exit = process.waitFor();
			if (exit != 0) {
				throw new RuntimeException("video processing failed!!");
			}

			return videoId;

		} catch (IOException ex) {
			throw new RuntimeException("Video processing fail!!");
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public List<Video> getVideosByCourseId(int courseId) {
		return videoRepository.findByCourseId(courseId);
	}

}
