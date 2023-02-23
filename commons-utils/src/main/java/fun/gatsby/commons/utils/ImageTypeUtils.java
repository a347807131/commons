package fun.gatsby.commons.utils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ImageTypeUtils {
    //  Returns the format of the image in the file 'f'.
    //  Returns null if the format is not known.
    public   static  String getFormatInFile(File f) {
        return  getFormatName(f);
    }

    //  Returns the format name of the image in the object 'o'.
    //  Returns null if the format is not known.
    private   static  String getFormatName(Object o) {
        try  {
            //  Create an image input stream on the image
            ImageInputStream iis  =  ImageIO.createImageInputStream(o);

            //  Find all image readers that recognize the image format
            Iterator<ImageReader> iter  =  ImageIO.getImageReaders(iis);
            if  ( ! iter.hasNext()) {
                //  No readers found
                return   null ;
            }

            //  Use the first reader
            ImageReader reader  =  iter.next();

            //  Close stream
            iis.close();

            //  Return the format name
            return  reader.getFormatName();
        }  catch  (IOException e) {
            //
        }

        //  The image could not be read
        return   null ;
    }
}
