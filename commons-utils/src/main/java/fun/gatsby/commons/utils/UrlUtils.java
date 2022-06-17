/*
+--------------------------------------------------------------------------
|   Mblog [#RELEASE_VERSION#]
|   ========================================
|   Copyright (c) 2014, 2015 mtons. All Rights Reserved
|   http://www.mtons.com
|
+---------------------------------------------------------------------------
*/
package fun.gatsby.commons.utils;

import java.net.InetAddress;

/**
 * @author Civin
 * url工具类
 */
public class UrlUtils {
    /**
     * 通过链接获得IP地址
     *
     * @param link
     * @return
     */
    public static InetAddress[] getHost(String link) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName("www.baidu.com");
            return addresses;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
