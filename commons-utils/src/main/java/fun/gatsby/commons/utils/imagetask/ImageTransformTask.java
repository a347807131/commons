package fun.gatsby.commons.utils.imagetask;

import fun.gatsby.commons.utils.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

@Slf4j
public class ImageTransformTask extends BaseTask {

    public static final FileFilter SUPPORTED_FILE_FILTER = file -> {
        if (file.isDirectory())
            return true;
        String lowerCasedName = file.getName().toLowerCase();
        return Const.SUPORTTED_FORMATS.stream().anyMatch(lowerCasedName::endsWith);
    };

    private final File inFile;
    private final String format;


    public ImageTransformTask(File inFile, File outFile, String format) {
        this.inFile = inFile;
        this.format = format;
        this.outFile = outFile;
        taskName = "格式转换: " + inFile.getName() + " to " + outFile.getAbsolutePath();
    }

    @Override
    public void doWork() throws IOException {
        FileUtils.forceMkdirParent(outFile);
        switch (format) {
            case "jp2": {
                BufferedImage bufferedImage = ImageIO.read(inFile);
                ImageIO.write(bufferedImage, "jpeg2000", outFile);
                break;
            }
            default: {
                BufferedImage bufferedImage = ImageIO.read(inFile);
                ImageIO.write(bufferedImage, format, outFile);
            }
        }
    }

}
