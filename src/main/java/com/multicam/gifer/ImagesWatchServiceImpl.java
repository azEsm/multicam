package com.multicam.gifer;

import com.multicam.image.GifImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ImagesWatchServiceImpl implements ImagesWatchService {
    private final Logger log = LoggerFactory.getLogger(ImagesWatchServiceImpl.class);
    private final String photoDirName;
    private final String gifDir;

    public ImagesWatchServiceImpl(String photoDirName, String gifDir) throws IOException {
        this.photoDirName = photoDirName;
        this.gifDir = gifDir;
    }

    @Override
    public void start() throws IOException {
        File photoDir = new ClassPathResource(photoDirName).getFile();
        log.info("photo dir {}", photoDir);

        String[] files = photoDir.list();
        log.info("files: {}", Arrays.toString(files));

        new GifImage(new ClassPathResource(gifDir).getFile(), Arrays.asList(files)).save();

        deleteImages(photoDir, files);
    }

    private void deleteImages(File photoDir, String[] files) {
        for (String fileName : files) {
            new File(photoDir, fileName).delete();
        }
    }
}
