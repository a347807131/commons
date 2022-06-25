package fun.gatsby.commons.utils;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FileTypeUtilsTest extends TestCase {

    public void testGetType() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("usecase/TEST-fun.gatsby.AppTest.xml");
        String type = FileTypeUtils.getType(is);
        Assert.assertEquals("xml", type);
        String type1 = FileTypeUtils.getType(ClassLoader.getSystemResourceAsStream("usecase/Screenshot_2018-07-17-20-20-05-51.png1"));
        Assert.assertEquals("png", type1);
    }

    public void test(){

        File file = new File("./");
        int i = countFileRecursively(file, null);
        LinkedList<File> list = new LinkedList<>();
        fetchFileRecursively(list,file, null);

    }

    public static void fetchFileRecursively(List<File> container, File dir, FileFilter filter) {
        File[] files = dir.listFiles(filter);
        if (files==null) return;
        for (File file : files) {
            if(file.isDirectory())
                fetchFileRecursively(container,file,filter);
            else
                container.add(file);
        }
    }


    public static int countFileRecursively(File dir, FilenameFilter filter){
        int count = 0;
        File[] files = dir.listFiles(filter);
        if (files==null) return count;
        for (File file : files) {
            if(file.isDirectory())
                count+=countFileRecursively(file,filter);
            else
                count++;
        }
        return count;
    }
}