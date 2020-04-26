package com.keray.common.service.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keray.common.BaseEntity;
import com.keray.common.service.ienum.ScheduleStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author by keray
 * date:2019/9/5 16:32
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_schedule")
public class SysScheduleModel extends BaseEntity {
    /**
     * 设备id
     */
    private String driverId;
    /**
     * beanName
     */
    private String beanName;
    /**
     * kz_cron表达式{"kz":"","cron":""}
     */
    private String kzCron;

    private ScheduleStatus status;


    /**
     * {
     * "name":"methodName",
     * "args":[
     * {
     * "clazz":""java.lang.String"",
     * "value":"123"
     * },
     * {
     * "clazz":""java.lang.Integer"",
     * "value":"123"
     * }
     * ]
     * }
     */
    private String methodDetail;

    /**
     * 重试次数 0 不重试
     */
    private Integer retryCount;

    /**
     * 重试间隔毫秒数
     */
    private Integer retryMillis;


    /**
     * 任务执行时间
     */
    private LocalDateTime execTime;

    /**
     * 任务计划执行时间
     */
    private LocalDateTime platExecTime;

    /**
     * 任务描述
     */
    private String scheduleDesc;
}
