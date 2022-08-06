package fun.gatsby.commons.lang;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的返回的消息类
 *
 * @author Civin
 */
@Data
public class Msg<T> implements Serializable {

    public static int CODE_OK = 100;
    public static int CODE_NOK = 200;
    public static String MSG_OK = "process successed";
    public static String MSG_NOK = "process failed";
    //返回的数据
    T data;
    //状态码 100-成功 200-失败
    private int code;
    //提示信息
    private String msg;

    private Msg() {
    }

    public static <T> Msg<T> of(String msg, int code, T data) {
        var dto = new Msg<T>();
        dto.data = data;
        dto.msg = msg;
        dto.code = code;
        return dto;
    }

    public static <T> Msg<T> ok(T data) {
        return of(MSG_OK, CODE_OK, data);
    }

    public static <T> Msg<T> nok(T data) {
        return of(MSG_NOK, CODE_NOK, data);
    }
}



