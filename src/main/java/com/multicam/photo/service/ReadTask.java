package com.multicam.photo.service;

import com.multicam.image.BmpImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

public class ReadTask implements Runnable {
    private static final char[] COMMAND = {'*', 'R', 'D', 'Y', '*'};
    private static final int WIDTH = 320; //640;
    private static final int HEIGHT = 240; //480;

    private final Logger log = LoggerFactory.getLogger(ReadTask.class);

    private final CommPortIdentifier portId;
    private final String destinationDirName;
    private final String imageName;
    private final CountDownLatch latch;

    public ReadTask(
            CommPortIdentifier portId,
            String destinationDirName,
            CountDownLatch latch
    ) {
        this.portId = portId;
        this.destinationDirName = destinationDirName;
        this.imageName = String.format("%s.%s", String.valueOf(portId.getPortType()), "bmp");
        this.latch = latch;
    }

    public void run() {
        awaitAllTasks();
        int[][] rgb = new int[HEIGHT][WIDTH];
        int[][] rgb2 = new int[WIDTH][HEIGHT];

        try {
            SerialPort serialPort = (SerialPort) portId.open("SimpleReadApp", 1000);
            InputStream inputStream = serialPort.getInputStream();

            serialPort.setSerialPortParams(
                    1000000,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
            );

            log.info("Looking for image");

            while (!isImageStart(inputStream, 0)) {}

            log.info("Found image: {}", imageName);

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    int temp = read(inputStream);
                    rgb[y][x] = ((temp & 0xFF) << 16) | ((temp & 0xFF) << 8) | (temp & 0xFF);
                }
            }

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    rgb2[x][y] = rgb[y][x];
                }
            }

            new BmpImage(destinationDirName, imageName, rgb2).save();

            log.info("Saved image: {}", imageName);
        } catch (Exception e) {
            log.error(String.format("image %s save error", imageName), e);
        }
    }

    private void awaitAllTasks() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            String message = String.format("Image %s. Await interrupted.", imageName);
            throw new IllegalStateException(message);
        }
    }

    private int read(InputStream inputStream) throws IOException {
        int temp = (char) inputStream.read(); //TODO blocked method. check available
        if (temp == -1) {
            throw new IllegalStateException("Exit");
        }
        return temp;
    }

    private boolean isImageStart(InputStream inputStream, int index) throws IOException {
        if (index < COMMAND.length) {
            return (COMMAND[index] == read(inputStream)) && isImageStart(inputStream, ++index);
        }
        return true;
    }
}