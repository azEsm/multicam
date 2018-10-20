package com.multicam.photo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.comm.CommPortIdentifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class PhotoServiceImpl implements PhotoService {

    //TODO properties
    private static final List<String> PORT_NAMES = Arrays.asList(
            "COM4",
            "COM5",
            "COM6",
            "COM7"
    );

    private final Logger log = LoggerFactory.getLogger(PhotoServiceImpl.class);
    private final String destinationDir;

    public PhotoServiceImpl(String destinationDir) {
        this.destinationDir = destinationDir;
    }

    @Override
    public void makePhoto() {
        CountDownLatch latch = new CountDownLatch(1);
        Set<Runnable> tasks = buildTasks(latch);
        runTasks(tasks, latch);
    }

    private Set<Runnable> buildTasks(CountDownLatch latch) {
        Set<Runnable> tasks = new HashSet<>();
        List<CommPortIdentifier> availablePorts = loadAvailablePorts();

        for (String portName : PORT_NAMES) {
            for (CommPortIdentifier port : availablePorts) {
                if (portName.equals(port.getName())) {
                    ReadTask task = new ReadTask(
                            port,
                            destinationDir,
                            latch
                    );
                    log.info("task created. port {}", port);
                    tasks.add(task);
                }
            }

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
    private void runTasks(Set<Runnable> tasks, CountDownLatch latch) {
        for (Runnable task : tasks) {
            new Thread(task).start();
        }
        latch.countDown();
    }
}
