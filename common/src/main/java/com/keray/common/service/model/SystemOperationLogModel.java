package com.keray.common.service.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keray.common.BaseEntity;
import com.keray.common.service.ienum.SystemOperationAction;
import lombok.*;

/**
 * @author by keray
 * date:2019/8/6 14:31
 * 系统操作记录
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_operation_log")
public class SystemOperationLogModel extends BaseEntity {
    private String tableName;
    private String operationName;
    private SystemOperationAction systemOperationAction;
    private String remark;
}
