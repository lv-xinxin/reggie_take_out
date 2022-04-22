package com.itheima.reggie.exceptions;

/**
 * 自定义业务异常
 */
public class CustomException extends RuntimeException {
    public CustomException(String message){
        super(message);
    }
    public CustomException(CustomExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMsg());
    }
}
