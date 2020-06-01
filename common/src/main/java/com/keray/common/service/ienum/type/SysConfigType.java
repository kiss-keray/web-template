package com.keray.common.service.ienum.type;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.keray.common.MybatisPlusEnum;
import lombok.Getter;

/**
 * @author by keray
 * date:2019/8/26 11:04
 */
public enum SysConfigType implements MybatisPlusEnum {
    //
    diamond("diamond动态配置", 0),
    page("页面配置", 1),
    conf("系统配置", 2);

    @Getter
    String desc;

    @EnumValue
    @Getter
    Integer code;

    SysConfigType(String desc, Integer code) {
        this.desc = desc;
        this.code = code;
    }
}
