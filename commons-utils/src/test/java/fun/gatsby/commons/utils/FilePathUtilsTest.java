package fun.gatsby.commons.utils;

import junit.framework.TestCase;

public class FilePathUtilsTest extends TestCase {

    public void test1() {

        String separator = "/|\\\\";
        String s = "C:/sers\\Gatsby\\scoop\\apps\\nginx\\current\\conf";
        String[] split = s.split(separator);
    }
}