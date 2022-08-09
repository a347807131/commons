package fun.gatsby.commons.utils;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class FileFetchUtilsTest extends TestCase {
    int times = 1024;

    public void testCountFileRecursively() {
        int count = 0;
        for (int i = 0; i < times; i++) {
            count += FileFetchUtils.countFileRecursively(new File("./"));
        }
        log.info("数量：{}", count);
    }

    public void testFetchFileRecursively() {
        List<File> list = new LinkedList<>();
        for (int i = 0; i < times; i++) {
            FileFetchUtils.fetchFileRecursively(list, new File("./"));
        }
        log.info("数量：{}",list.size());

    }
}