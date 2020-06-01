package com.keray.common.service.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keray.common.BaseEntity;
import com.keray.common.service.ienum.type.SysConfigType;
import lombok.*;

/**
 * @author by keray
 * date:2019/7/29 9:59
 * 系统通用配置
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_config")
public class SysConfigModel extends BaseEntity {
    private SysConfigType type;
    @TableField(value = "`key`")
    private String key;
    private String value;
    /**
     * 状态|0 无效|1 有效
     */
    private Integer status;

    private String configDesc;
}
