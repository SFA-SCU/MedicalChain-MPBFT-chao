package com.pancake.util;

import com.pancake.entity.util.Result;
import com.pancake.enums.StatEnum;

/**
 * Created by chao on 2017/6/13.
 * 控制返回结果，返回对象为Result对象。
 */
public class ResultUtil {

    /**
     * 设置响应成功时的 Result 对象的内容
     * @param object 要返回的具体内容
     * @return 返回带有状态码，状态信息，以及具体返回对象的 Result 对象
     */
    public static Result success(Object object) {
        Result result = new Result();
        result.setStatus(StatEnum.SUCCESS.getState());
        result.setMsg(StatEnum.SUCCESS.getStateInfo());
        result.setData(object);
        return result;
    }

    /**
     * 当无返回内容时，使用该方法
     * @return 返回值为 null 的 Result 对象
     */
    public static Result success() {
        return success(null);
    }


    /**
     * 设置响应失败时的 Result 对象的内容
     * @param status 错误状态码
     * @param msg 错误信息
     * @return 带有错误状态码，错误信息，以及值为 null 的 Result 对象
     */
    public static Result error(Integer status, String msg) {
        Result result = new Result();
        result.setStatus(status);
        result.setMsg(msg);
        return result;
    }
}