package com.multicam.photo.service;

import com.multicam.gifer.ImagesWatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.comm.CommPortIdentifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PhotoServiceImpl implements PhotoService {

    private final Logger log = LoggerFactory.getLogger(PhotoServiceImpl.class);
    private final String destinationDir;
    private final ImagesWatchService watchService;

    public PhotoServiceImpl(String destinationDir, ImagesWatchService watchService) {
        this.destinationDir = destinationDir;
        this.watchService = watchService;
    }

    @Override
    public void makePhoto() {
        CountDownLatch latch = new CountDownLatch(1);
        Set<Callable<Void>> tasks = buildTasks(latch);
        try {
            runTasksAndWait(tasks, latch);
            watchService.start();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Set<Callable<Void>> buildTasks(CountDownLatch latch) {
        Set<Callable<Void>> tasks = new HashSet<>();
        List<CommPortIdentifier> availablePorts = loadAvailablePorts();

        for (CommPortIdentifier port : availablePorts) {
            ReadTask task = new ReadTask(
                    port,
                    destinationDir,
                    latch
            );
            log.info("task created. port {}", port.getName());
            tasks.add(task);
        }

        return tasks;
    }

    @SuppressWarnings("unchecked")
    private List<CommPortIdentifier> loadAvailablePorts() {
        return Collections.<CommPortIdentifier>list(CommPortIdentifier.getPortIdentifiers());
    }

    /**
     * Simultaneous run all the tasks
     *
     * @param tasks
     * @param latch
     */
    private void runTasksAndWait(Set<Callable<Void>> tasks, CountDownLatch latch) throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newFixedThreadPool(4);
        List<Future<Void>> tasksResults = es.invokeAll(tasks);
        latch.countDown();

        for (Future<Void> taskResult : tasksResults) {
            taskResult.get();
        }
    }
}
