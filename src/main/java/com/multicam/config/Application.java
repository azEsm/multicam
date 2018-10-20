package com.multicam.config;

import com.multicam.gifer.ImagesWatchService;
import com.multicam.gifer.ImagesWatchServiceImpl;
import com.multicam.photo.service.PhotoService;
import com.multicam.photo.service.PhotoServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public PhotoService photoService(@Value("${photo.dir}") String destinationDir) {
        return new PhotoServiceImpl(destinationDir);
    }

    @Bean
    public ImagesWatchService watchService(
            @Value("${photo.dir}") String photoDir,
            @Value("${gif.dir}") String gifDir
    ) {
        return new ImagesWatchServiceImpl(photoDir, gifDir);
    }
}