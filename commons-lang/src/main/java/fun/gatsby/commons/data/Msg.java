package fun.gatsby.commons.data;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的返回的类
 *
 * @author Civin
 */
@Data
public class Msg<T extends Serializable> {

    public static int CODE_SUCCESS = 100;
    public static int CODE_FAIL = 200;
    public static String MSG_SUCCESS = "process successed";
    public static String MSG_FAIL = "process failed";
    //返回的数据
    T date;
    //状态码 100-成功 200-失败
    private int code;
    //提示信息
    private String msg;

    public static <T extends Serializable> Msg<T> success(T data) {
        Msg<T> msg = new Msg<>();
        msg.code = CODE_SUCCESS;
        msg.msg = MSG_SUCCESS;
        msg.date = data;
        return msg;
    }

    public static <T extends Serializable> Msg<T> fail(T data) {
        Msg<T> msg = new Msg<>();
        msg.code = CODE_FAIL;
        msg.msg = MSG_FAIL;
        msg.date = data;
        return msg;
    }
}



