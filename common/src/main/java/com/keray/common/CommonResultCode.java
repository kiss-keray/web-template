package com.keray.common;

import lombok.Getter;

/**
 * @author by keray
 * date:2019/8/1 11:54
 * 返回code
 */
public enum CommonResultCode implements ResultCode{
    //全局异常
    unknown(-1, "网络繁忙"),
    illegalArgument(10001, "数据不合法"),
    dataChangeError(10002, "数据更新失败"),
    limitedAccess(10003, "访问受限"),
    argumentNotPresent(10004, "参数缺失"),
    dataNotAllowDelete(11001, "数据被约束，无法删除")
    ;


    @Getter
    private final int code;
    @Getter
    private final String message;

    CommonResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
