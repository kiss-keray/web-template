create table sys_config
(
    id          varchar(64)      not null comment '主键'
        primary key,
    create_time datetime         null comment '创建时间',
    modify_time datetime         null comment '修改时间',
    deleted     bit default b'0' null comment '0 未删除/ 1 删除',
    delete_time datetime         null comment '删除时间',
    create_by   varchar(64)      null comment '创建来源',
    modify_by   varchar(64)      null comment '修改来源',
    `key`       varchar(128)     null comment '配置key',
    value       longtext         null comment 'value',
    status      int              null comment '状态|0 无效|1 有效',
    type        int default 1    null comment '配置类型',
    config_desc varchar(256)     null comment '描述'
)
    comment '系统配置（v3）' charset = utf8mb4;

create index sys_config__index_key
    on sys_config (`key`);

create table sys_schedule
(
    id             varchar(64)      not null comment '主键'
        primary key,
    create_time    datetime         null comment '创建时间',
    modify_time    datetime         null comment '修改时间',
    deleted        bit default b'0' null comment '0 未删除/ 1 删除',
    delete_time    datetime         null comment '删除时间',
    create_by      varchar(64)      null comment '创建来源',
    modify_by      varchar(64)      null comment '修改来源',
    status         varbinary(64)    null comment '任务状态',
    driver_id      varchar(256)     null comment '设备id',
    bean_name      varchar(256)     null comment 'beanName',
    kz_cron        varchar(128)     null comment 'kz_cron表达式{"kz":[""],"cron":[]}',
    method_detail  varchar(1024)    null comment '方法签名',
    retry_count    int              null comment '重试次数',
    retry_millis   int default 1000 null comment '任务重试间隔毫秒',
    exec_time      datetime         null comment '任务执行时间',
    plat_exec_time datetime         null comment '计划执行时间',
    schedule_desc  varchar(256)     null comment '任务描述'
)
    comment '系统分布式任务（v3）' charset = utf8mb4;

create index index_status
    on sys_schedule (status);

