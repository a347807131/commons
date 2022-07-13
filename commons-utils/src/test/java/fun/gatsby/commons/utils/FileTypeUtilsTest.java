package fun.gatsby.commons.utils;

import junit.framework.TestCase;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;

public class FileTypeUtilsTest extends TestCase {

    public void testGetType() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("usecase/TEST-fun.gatsby.AppTest.xml");
        String type = FileTypeUtils.getType(is);
        Assert.assertEquals("xml", type);
        String type1 = FileTypeUtils.getType(ClassLoader.getSystemResourceAsStream("usecase/Screenshot_2018-07-17-20-20-05-51.png1"));
        Assert.assertEquals("png", type1);
    }
}