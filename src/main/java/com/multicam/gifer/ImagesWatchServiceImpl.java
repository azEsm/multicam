package com.multicam.gifer;

import com.multicam.image.GifImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;

public class ImagesWatchServiceImpl implements ImagesWatchService {
    private final Logger log = LoggerFactory.getLogger(ImagesWatchServiceImpl.class);
    private final Path photoDir;
    private final String gifDir;

    public ImagesWatchServiceImpl(String photoDir, String gifDir) throws IOException {
        this.photoDir = new ClassPathResource(photoDir).getFile().toPath();
        this.gifDir = gifDir;
    }

    @Override
    public void start() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            photoDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            processEvents(watcher);
        } catch (IOException e) {
            String message = String.format("watching directory %s error", photoDir);
            log.error(message, e);
        }
    }

    private void processEvents(WatchService watcher) throws IOException {
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                String[] files = photoDir.toFile().list();

                //TODO properties
                if (files == null || files.length < 4) {
                    continue;
                }

                new GifImage(new ClassPathResource(gifDir).getFile(), Arrays.asList(files)).save();

                deleteImages(files);
            }
        }
    }

    private void deleteImages(String[] files) {
        for (String fileName : files) {
            new File(photoDir.toFile(), fileName).delete();
        }
    }
}
