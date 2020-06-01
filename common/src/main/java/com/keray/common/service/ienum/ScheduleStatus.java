package com.keray.common.service.ienum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.keray.common.MybatisPlusEnum;
import lombok.Getter;

/**
 * @author by keray
 * date:2020/4/26 12:09 下午
 */
public enum ScheduleStatus implements MybatisPlusEnum {
    //
    waitSubmit("等待提交", "waitSubmit"),
    waitExec("等待执行", "waitExec"),
    exec("执行中", "exec"),
    success("成功", "success"),
    fail("失败", "fail"),
    waitRetry("等待重试", "waitRetry"),
    cancel("取消", "cancel")
    ;
    @Getter
    String desc;

    @EnumValue
    @Getter
    String code;

    ScheduleStatus(String desc, String code) {
        this.desc = desc;
        this.code = code;
    }
}
