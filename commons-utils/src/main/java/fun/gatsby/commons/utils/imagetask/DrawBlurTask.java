package fun.gatsby.commons.utils.imagetask;

import fun.gatsby.commons.utils.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
public class DrawBlurTask extends BaseTask {

    private final File inFile;
    private final File blurImageFile;

    public DrawBlurTask(File inFile, File outFile, File blurImageFile) {
        this.inFile = inFile;
        this.outFile = outFile;
        this.blurImageFile = blurImageFile;
        taskName = "绘制水印图: " + inFile.getName() + " to " + outFile.getAbsolutePath();
    }

    @Override
    public void doWork() throws IOException {
        String format = inFile.getName().substring(inFile.getName().lastIndexOf(".") + 1);
        BufferedImage blurBufferedImage = ImageIO.read(blurImageFile);
        BufferedImage bufferedImage = ImageIO.read(inFile);
        float scale = bufferedImage.getHeight() / (4f * blurBufferedImage.getHeight());
        ImageUtils.drawBlurPic(bufferedImage, blurBufferedImage, scale);
        switch (format) {
            case "jp2": {
                ImageIO.write(bufferedImage, "jpeg2000", outFile);
            }
            default: {
                ImageIO.write(bufferedImage, format, outFile);
            }
        }
    }
}
