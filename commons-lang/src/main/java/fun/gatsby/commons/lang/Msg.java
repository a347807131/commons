package fun.gatsby.commons.lang;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的返回的类
 *
 * @author Civin
 */
@Data
public class Msg<T> implements Serializable {

    public static int CODE_SUCCESS = 100;
    public static int CODE_FAIL = 200;
    public static String MSG_SUCCESS = "process successed";
    public static String MSG_FAIL = "process failed";
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
        return of(MSG_SUCCESS, CODE_SUCCESS, data);
    }

    public static <T> Msg<T> fail(T data) {
        return of(MSG_FAIL, CODE_SUCCESS, data);
    }
}



