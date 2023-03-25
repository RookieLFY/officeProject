package com.atguigu.common.config.exception;

import com.atguigu.common.result.ResultCodeEnum;

public class GuiGuException extends RuntimeException{
    private Integer code;
    private String message;
    public GuiGuException(Integer code,String message){
        super(message);
        this.code=code;
        this.message=message;
    }

     /** 接收枚举类型对象
      * *@param resultCodeEnum
     */
    public GuiGuException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }

    @Override
    public String toString() {
        return "GuliException{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }
}
