package fun.gatsby.commons.utils.utils;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Objects;

@Slf4j
public class ImageUtils {
    public static void drawBlurPic(BufferedImage oriBufferedImage, BufferedImage blurBufferedImage) {
        Point point = new Point((oriBufferedImage.getWidth() - blurBufferedImage.getWidth()) / 2, (oriBufferedImage.getHeight() - blurBufferedImage.getHeight()) / 2);
        Graphics g = oriBufferedImage.getGraphics();
        g.drawImage(blurBufferedImage, point.x, point.y, null);
        g.dispose();
    }

    public static void drawBlurPic(BufferedImage oriBufferedImage, BufferedImage blurBufferedImage, float scale) throws IOException {
        BufferedImage scaledBlurBufferedImage = Thumbnails.of(blurBufferedImage).scale(scale).asBufferedImage();
        drawBlurPic(oriBufferedImage, scaledBlurBufferedImage);
    }

    public static void transformImgToJpg(BufferedImage bufferedImage, OutputStream outputStream, int limit) throws IOException {
        byte[] bytes = imageToBytes(bufferedImage);
        // 把图片读入到内存中
        if (limit > 0) {
            bytes = PicCompressUtils.compressPicForScale(bytes, limit);
        }
        IOUtils.write(bytes, outputStream);
    }

    public static void transformImgToJp2(BufferedImage bufferedImage, File outFile, int limit) throws IOException {
        float fsize = bufferedImage.getData().getDataBuffer().getSize() / (1024 * 1024f);
        float oriFileSizeM = fsize;
        float x = fsize;
        float encoding = (float) (1.395e-9 * Math.pow(x, 3) + 5.408e-6 * Math.pow(x, 2) - 2.187e-3 * x + 0.2709);
        float limitM = limit / 1024f;
        if (limitM == 0) {
            OutputStream os = Files.newOutputStream(outFile.toPath());
            ImageIO.write(bufferedImage, "JPEG2000", os);
            os.close();
            return;
        }
        int compressTime = 0;
        while (fsize > limitM || fsize < limitM * 0.8) {
            compressTime += 1;
            OutputStream os = Files.newOutputStream(outFile.toPath());
            long s = System.currentTimeMillis();
            ImageUtils.transformImgToJp2(bufferedImage, os, 0.5f, encoding);
            os.close();
            fsize = outFile.length() / (1024 * 1024f);
            log.debug("压缩次数{},输出文件大小{}m,原文件大小{}m,编码率{},耗时{}s,文件名{}",
                    compressTime,
                    fsize,
                    oriFileSizeM,
                    encoding,
                    (System.currentTimeMillis() - s) / 1000f,
                    outFile.getAbsolutePath()
            );
            if (compressTime < 2)
                encoding = (limitM * 0.95f) / fsize * encoding;
            else if (compressTime < 5) {
                System.gc();
                if (fsize > limitM)
                    encoding *= 0.95;
                else if (fsize < limitM * 0.8)
                    encoding *= 1.05;
                else break;
            } else
                throw new IOException("压缩次数过多，为防爆内存，异常推出");
        }
    }

    public static void transformImgToJp2(BufferedImage bufferedImage, OutputStream outputStream, float quality, float encodingRate) {
        try (
                ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
        ) {
            String name = null;
            ImageWriter writer = null;
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("JPEG2000");
            while (!Objects.equals(name, "com.github.jaiimageio.jpeg2000.impl.J2KImageWriter")) {
                writer = writers.next();
                name = writer.getClass().getName();
            }
            writer.setOutput(ios);
            J2KImageWriteParam param = (J2KImageWriteParam) writer.getDefaultWriteParam();
            IIOImage ioimage = new IIOImage(bufferedImage, null, null);
//            param.setSOP(true);
//            param.setWriteCodeStreamOnly(true);
//            param.setProgressionType("layer");
//            param.setLossless(true);
            param.setCompressionMode(J2KImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("JPEG2000");
            if (quality > 0)
                param.setCompressionQuality(quality);
            if (encodingRate != 0) {
                param.setEncodingRate(encodingRate);
            }
            writer.write(null, ioimage, param);
            writer.dispose();
            ios.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void transformImgToJp2(BufferedImage bufferedImage, OutputStream outputStream) {
        transformImgToJp2(bufferedImage, outputStream, 0, 0);
    }

    public static byte[] imageToBytes(BufferedImage bufferedImage) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    public static BufferedImage bytesToImage(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            return ImageIO.read(byteArrayInputStream);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}