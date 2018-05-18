package com.nix.common;

import java.util.Map;

/**
 * 接口统一返回对象
 *
 * @author 11723*/
public class ReturnObject<T extends Object> {
    /**
     * 状态码
     * */
    private Integer status;
    /**
     * 消息
     */
    private String msg;
    /**
     * 返回内容
     */
    private T data;

    /**
     * 额外json
     * */
    private Map additionalData;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map additionalData) {
        this.additionalData = additionalData;
    }

    @Override
    public String toString() {
        return "ReturnObject{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", additionalData=" + additionalData +
                '}';
    }
}
