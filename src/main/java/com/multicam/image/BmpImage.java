package com.multicam.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BmpImage implements Image {
    private final String fileName;
    private final String outputDir;
    private final int[][] rgbValues;
    private final byte[] bytes;

    public BmpImage(String outputDir, String filename, int[][] rgbValues) {
        this.fileName = filename;
        this.outputDir = outputDir;
        this.rgbValues = rgbValues;
        bytes = new byte[54 + 3 * rgbValues.length * rgbValues[0].length];
    }

    @Override
    public void save() {
        try (FileOutputStream fos = new FileOutputStream(new File(outputDir, fileName))) {
            saveFileHeader();
            saveInfoHeader(rgbValues.length, rgbValues[0].length);
            saveBitmapData(rgbValues);

            fos.write(bytes);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void saveFileHeader() {
        bytes[0] = 'B';
        bytes[1] = 'M';

        bytes[5] = (byte) bytes.length;
        bytes[4] = (byte) (bytes.length >> 8);
        bytes[3] = (byte) (bytes.length >> 16);
        bytes[2] = (byte) (bytes.length >> 24);

        //data offset
        bytes[10] = 54;
    }

    private void saveInfoHeader(int height, int width) {
        bytes[14] = 40;

        bytes[18] = (byte) width;
        bytes[19] = (byte) (width >> 8);
        bytes[20] = (byte) (width >> 16);
        bytes[21] = (byte) (width >> 24);

        bytes[22] = (byte) height;
        bytes[23] = (byte) (height >> 8);
        bytes[24] = (byte) (height >> 16);
        bytes[25] = (byte) (height >> 24);

        bytes[26] = 1;

        bytes[28] = 24;
    }

    private void saveBitmapData(int[][] rgbValues) {
        for (int i = 0; i < rgbValues.length; i++) {
            writeLine(i, rgbValues);
        }
    }

    private void writeLine(int row, int[][] rgbValues) {
        final int offset = 54;
        final int rowLength = rgbValues[row].length;
        for (int i = 0; i < rowLength; i++) {
            int rgb = rgbValues[row][i];
            int temp = offset + 3 * (i + rowLength * row);

            bytes[temp + 2] = (byte) (rgb >> 16);
            bytes[temp + 1] = (byte) (rgb >> 8);
            bytes[temp] = (byte) rgb;
        }
    }
}

