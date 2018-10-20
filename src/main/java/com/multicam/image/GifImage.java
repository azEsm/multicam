package com.multicam.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class GifImage {
    private final Logger log = LoggerFactory.getLogger(GifImage.class);
    private ImageWriter gifWriter;
    private ImageWriteParam imageWriteParam;
    private IIOMetadata imageMetaData;
    private List<RenderedImage> sourceImages;
    private File outputDir;

    public GifImage(
            File outputDir,
            List<String> sourceImages
    ) throws IOException {
        if (CollectionUtils.isEmpty(sourceImages)) {
            return;
        }
        // my method to create a writer
        gifWriter = getWriter();
        imageWriteParam = gifWriter.getDefaultWriteParam();

        this.outputDir = outputDir;

        BufferedImage firstImage = ImageIO.read(new File(sourceImages.get(0)));
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(firstImage.getType());

        imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

        String metaFormatName = imageMetaData.getNativeMetadataFormatName();

        IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");

        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(10)); //TODO ???
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
        commentsNode.setAttribute("CommentExtension", "Created by MAH");

        IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");

        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");

        //loop repeatedly
        int loop = 0;

        child.setUserObject(
                new byte[]{
                        0x1,
                        (byte) (loop & 0xFF),
                        (byte) ((loop >> 8) & 0xFF)
                }
        );
        appExtensionsNode.appendChild(child);

        imageMetaData.setFromTree(metaFormatName, root);
        gifWriter.prepareWriteSequence(null);
        this.sourceImages = new ArrayList<>();
        for (String image : sourceImages) {
            this.sourceImages.add(ImageIO.read(new File(image)));
        }

    }

    public void save() {
        checkOutputDirectory();
        DateFormat format = new SimpleDateFormat("dd.MM.yyyyHH:mm:ss");
        String fileName = String.format("%s.%s", format.format(new Date()), "gif");
        try (OutputStream outputStream = new FileOutputStream(new File(outputDir, fileName))) {
            gifWriter.setOutput(outputStream);

            ListIterator<RenderedImage> iterator = sourceImages.listIterator();
            while (iterator.hasNext()) {
                gifWriter.writeToSequence(new IIOImage(iterator.next(), null, imageMetaData), imageWriteParam);
            }

            while (iterator.hasPrevious()) {
                gifWriter.writeToSequence(new IIOImage(iterator.previous(), null, imageMetaData), imageWriteParam);
            }

            gifWriter.endWriteSequence();
        } catch (IOException e) {
            log.error("Writing GIF error", e);
        }
    }

    private void checkOutputDirectory() {
        if (outputDir.exists()) {
            return;
        }

        if (!outputDir.mkdirs()) {
            throw new IllegalStateException(String.format("Creating directory %s error", outputDir));
        }
    }

    /**
     * Returns the first available GIF ImageWriter using
     * ImageIO.getImageWritersBySuffix("gif").
     *
     * @return a GIF ImageWriter object
     * @throws IIOException if no GIF image writers are returned
     */
    private static ImageWriter getWriter() throws IIOException {
        Iterator<ImageWriter> availableWriters = ImageIO.getImageWritersBySuffix("gif");
        if (!availableWriters.hasNext()) {
            throw new IIOException("No GIF Image Writers Exist");
        }
        return availableWriters.next();
    }

    /**
     * Returns an existing child node, or creates and returns a new child node (if
     * the requested node does not exist).
     *
     * @param rootNode the <tt>IIOMetadataNode</tt> to search for the child node.
     * @param nodeName the name of the child node.
     * @return the child node, if found or a new node created with the given name.
     */
    private static IIOMetadataNode getNode(
            IIOMetadataNode rootNode,
            String nodeName
    ) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return (node);
    }
}
