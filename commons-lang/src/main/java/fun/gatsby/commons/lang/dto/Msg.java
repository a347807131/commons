package fun.gatsby.commons.lang.dto;

import fun.gatsby.commons.lang.IResult;
import fun.gatsby.commons.lang.IStatusEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用的返回的消息类
 *
 * @author Civin
 */
@Data
public class Msg<T> implements Serializable, IResult {

    public static int CODE_OK = 200;
    public static int CODE_NOK = 500;
    public static String MSG_OK = "process successed";
    public static String MSG_NOK = "process failed";
    //返回的数据
    T data;
    //状态码 200-成功 500-失败
    private int code;
    //提示信息
    private String msg;

    Msg() {
    }

    public static <T> Msg<T> of(String msg, int code) {
        var dto = new Msg<T>();
        dto.msg = msg;
        dto.code = code;
        return dto;
    }

    public static <T> Msg<T> of(String msg, int code, T data) {
        var dto = new Msg<T>();
        dto.data = data;
        dto.msg = msg;
        dto.code = code;
        return dto;
    }

    public static <T> Msg<T> ok() {
        return of(MSG_OK, CODE_OK);
    }

    public static <T> Msg<T> ok(T data) {
        return of(MSG_OK, CODE_OK, data);
    }

    public static <T> Msg<T> nok() {
        return of(MSG_NOK, CODE_NOK);
    }

    public static <T> Msg<T> nok(IStatusEnum statusEnum) {
        return of(statusEnum.getMessage(), statusEnum.getCode());
    }

    public static <T> Msg<T> nok(T data) {
        return of(MSG_NOK, CODE_NOK, data);
    }
}



