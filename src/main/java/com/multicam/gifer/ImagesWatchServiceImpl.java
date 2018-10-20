package com.multicam.gifer;

import com.multicam.image.GifImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.stream.Collectors;

public class ImagesWatchServiceImpl implements ImagesWatchService {
    private final Logger log = LoggerFactory.getLogger(ImagesWatchServiceImpl.class);
    private final Path photoDir;
    private final String gifDir;

    public ImagesWatchServiceImpl(String photoDir, String gifDir) {
        this.photoDir = Paths.get(photoDir);
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

                List<String> fileNames = Files.list(photoDir).map(Path::toString).collect(Collectors.toList());
                long filesCount = fileNames.size();

                if (filesCount < 4) {
                    continue;
                }

                new GifImage(new File(gifDir), fileNames).save();
            }
        }
    }
}
