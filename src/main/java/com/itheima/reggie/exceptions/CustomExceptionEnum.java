package com.itheima.reggie.exceptions;

public enum CustomExceptionEnum {
    ASSOCIATION_DISH(10000, "已关联菜品,删除失败"),
    ASSOCIATION_SETMEAL(10001, "已关联套餐,删除失败"),
    SETMEAL_SELLING(10002, "套餐正在售卖中，无法删除");
    /**
     * 错误代码
     */
    private Integer code;
    /**
     * 错误信息
     */
    private String msg;


    CustomExceptionEnum() {
    }

    CustomExceptionEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
