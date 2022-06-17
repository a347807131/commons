package fun.gatsby.commons.utils;

import java.util.Random;

public class MD5 {

    /**
     * 定义字符集
     *
     * @param
     * @return
     */
    private static char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
            'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z'};    //72个字符集

    /**
     * @param passLength 随机密码长度
     * @return 随机密码数组
     */
    public static String getPasswords(int passLength) {
        String passwords = "";// 新建一个长度为指定需要密码个数的字符串数组
        Random random = new Random();
        StringBuilder password = new StringBuilder("");// 保存生成密码的变量
        for (int m = 1; m <= passLength; m++) {// 内循环 从1开始到密码长度 正式开始生成密码
            password.append(chars[random.nextInt(62)]);// 为密码变量随机增加上面字符中的一个
        }
        passwords = password.toString();// 将生成出来的密码赋值给密码数组
        return passwords;
    }
}
