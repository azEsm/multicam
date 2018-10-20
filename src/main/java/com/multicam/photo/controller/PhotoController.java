package com.multicam.photo.controller;

import com.multicam.photo.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class PhotoController {
    private final PhotoService photoService;
    private final String gifsDirName;

    public PhotoController(@Autowired PhotoService photoService, @Value("${gif.dir}") String gifsDirName) {
        this.photoService = photoService;
        this.gifsDirName = gifsDirName;
    }

    @RequestMapping(value = "photo", method = RequestMethod.GET)
    public void makePhoto() {
        photoService.makePhoto();
    }

    @RequestMapping(value = "gifs", method = RequestMethod.GET)
    public List<String> gifs() throws IOException {
        File gifsDir = new ClassPathResource(gifsDirName).getFile();
        if (!gifsDir.exists()) {
            return Collections.emptyList();
        }

        String[] gifsFileNames = gifsDir.list();

        if (gifsFileNames == null || gifsFileNames.length < 1) {
            return Collections.emptyList();
        }

        return Arrays.asList(gifsFileNames);
    }
}
