package fun.gatsby.commons.utils;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件类型判断类
 * * @author Civin
 * * @create 2018-02-20 16:19
 */
public class FileTypeUtils {

    /**
     * 将文件头转换成16进制字符串
     *
     * @param b 原生byte
     * @return 16进制字符串
     */
    private static String bytesToHexString(byte[] b) {

        StringBuilder stringBuilder = new StringBuilder();
        if (b == null || b.length <= 0) {
            return null;
        }
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xFF;
            String str = Integer.toHexString(v);
            if (str.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }

    /**
     * 得到文件头
     *
     * @return 文件头
     * @throws IOException
     */
    private static String getFileContent(InputStream inputStream) throws IOException {

        byte[] b = new byte[28];
        inputStream.read(b, 0, 28);
        return bytesToHexString(b);
    }

    /**
     * 判断文件类型
     *
     * @return 文件类型
     */
    public static String getType(InputStream inputStream) throws IOException {

        String fileHead = getFileContent(inputStream);
        if (fileHead == null || fileHead.length() == 0) {
            return null;
        }

        fileHead = fileHead.toUpperCase();

        FileType[] fileTypes = FileType.values();
        for (FileType type : fileTypes) {
            if (fileHead.startsWith(type.getValue())) {
                return type.toString().toLowerCase();
            }
        }
        return null;
    }

    public static String getType(String filePath) throws IOException {
        return getType(new FileInputStream(filePath));
    }

    /**
     * 文件类型枚取
     */
    public enum FileType {

        JPG("FFD8FF"),
        PNG("89504E47"),
        GIF("47494638"),
        TIFF("49492A00"),
        BMP("424D"),
        DWG("41433130"),
        PSD("38425053"),
        RTF("7B5C727466"),
        XML("3C3F786D6C"),
        HTML("68746D6C3E"),
        EML("44656C69766572792D646174653A"),
        DBX("CFAD12FEC5FD746F"),
        PST("2142444E"),
        XLS_DOC("D0CF11E0"),
        MDB("5374616E64617264204A"),
        WPD("FF575043"),
        EPS("252150532D41646F6265"),
        PDF("255044462D312E"),
        QDF("AC9EBD8F"),
        PWL("E3828596"),
        ZIP("504B0304"),
        RAR("52617221"),
        WAV("57415645"),
        AVI("41564920"),
        RAM("2E7261FD"),
        RM("2E524D46"),
        MPG("000001BA"),
        MOV("6D6F6F76"),
        ASF("3026B2758E66CF11"),
        MID("4D546864");


        private String value = "";

        FileType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
