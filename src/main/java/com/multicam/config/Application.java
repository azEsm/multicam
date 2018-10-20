package com.multicam.config;

import com.multicam.gifer.ImagesWatchService;
import com.multicam.gifer.ImagesWatchServiceImpl;
import com.multicam.photo.controller.PhotoController;
import com.multicam.photo.service.PhotoService;
import com.multicam.photo.service.PhotoServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.io.IOException;

@SpringBootApplication
@ComponentScan(basePackageClasses = PhotoController.class)
@Import(WebConfig.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public PhotoService photoService(@Value("${photo.dir}") String destinationDir, ImagesWatchService watchService) {
        return new PhotoServiceImpl(destinationDir, watchService);
    }

    @Bean
    public ImagesWatchService watchService(
            @Value("${photo.dir}") String photoDir,
            @Value("${gif.dir}") String gifDir
    ) throws IOException {
        return new ImagesWatchServiceImpl(photoDir, gifDir);
    }
}
