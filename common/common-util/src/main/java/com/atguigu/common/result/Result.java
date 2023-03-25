package com.atguigu.common.result;

import lombok.Data;
@Data
public class Result<T>{
    private Integer code;//状态码
    private String message;//代表返回的信息
    private T data;//数据

    //私有化
    private Result(){
    }
    //封装返回的数据
    public static <T> Result<T> build(T body, ResultCodeEnum resultCodeEnum) {
        Result<T> result = new Result<>();
        //封装数据
        if(body!=null){
            result.setData(body);
        }
        //状态码
        result.setCode(resultCodeEnum.getCode());
        //相关的返回信息
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }
    //成功
    //无数据
    public static<T> Result<T> ok(){
        return build(null,ResultCodeEnum.SUCCESS);
    }
    //有数据
    public static<T> Result<T> ok(T data){
        return build(data,ResultCodeEnum.SUCCESS);
    }
    //失败
    //无数据
    public static<T> Result<T> fail(){
        return build(null,ResultCodeEnum.FAIL);
    }
    //有数据
    public static<T> Result<T> fail(T data){
        return build(data,ResultCodeEnum.FAIL);
    }
    //扩展：操作（正确或者失败名字）
    public Result<T> message(String msg){
        this.setMessage(msg);
        return this;
    }
    //扩展：操作（代码）
    public Result<T> code(Integer code){
        this.setCode(code);
        return this;
    }
}
