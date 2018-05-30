package com.pancake.exception;

/**
 * 自定义 User RuntimeException，事务只有抛出 RuntimeException 时才会回滚
 * Created by m on 2017/6/9.
 */
public class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}
