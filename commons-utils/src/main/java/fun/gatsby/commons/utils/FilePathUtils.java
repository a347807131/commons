package fun.gatsby.commons.utils;

public class FilePathUtils {

    public static String CURRENT_OS_NAME = System.getProperty("os.name");

    public static String FILE_SEPARATOR_REGEX = "/|\\\\";

    public static String getOperatorSystemName() {
        return System.getProperty("os.name");
    }
}
