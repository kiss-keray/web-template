package com.keray.common.service.ienum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.keray.common.MybatisPlusEnum;
import lombok.Getter;

/**
 * @author by keray
 * date:2019/8/6 14:32
 */
public enum SystemOperationAction implements MybatisPlusEnum {
    //
    create("创建", 0),
    update("修改", 1),
    delete("删除", 2),
    other("其他", 3);
    @Getter
    String desc;

    @EnumValue
    @Getter
    Integer code;

    SystemOperationAction(String desc, Integer code) {
        this.desc = desc;
        this.code = code;
    }
}
