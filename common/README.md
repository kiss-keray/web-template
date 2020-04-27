### 配置
```yaml
keray: 
  schedule: true # 定时任务配置
  api: 
    json: 
      open: true # json解析
      global-switch: # 全局开启json解析
    data: true # 接口qps配置
    time: true # 接口时长统计
    
```

## cache使用
```yaml
spring:
  cache: 
    type: redis
    ehcache:
      config: xxxx.xml
```
#### 例子：
```xml
<ehcache>

    <diskStore path="java.io.tmpdir"/>
    <defaultCache maxElementsInMemory="10000" eternal="false"
                  timeToIdleSeconds="600" timeToLiveSeconds="600" overflowToDisk="false"/>

    <!--  大对象，实时性高，更新率高，10分钟存活时间 -->
    <cache name="cache:tree_data_cache" timeToLiveSeconds="600" />

</ehcache>

```

## diamond配置
```java
@Service("questionSysConfig")
public class QuestionSysConfig extends SysConfigService {
    @DiamondSupport(key = "config-title",name = "名称")
    private String title;
}

```
#### sql
```sql
-- auto-generated definition
create table sys_config
(
    id          varchar(64)      not null comment '主键'
        primary key,
    create_time datetime         null comment '创建时间',
    modify_time datetime         null comment '修改时间',
    deleted     bit default b'0' null comment '0 未删除/ 1 删除',
    delete_time datetime         null comment '删除时间',
    create_by   varchar(64)      null comment '创建来源',
    update_by   varchar(64)      null comment '修改来源',
    `key`       varchar(128)     null comment '配置key',
    value       varchar(4048)    null comment 'value',
    status      int              null comment '状态|0 无效|1 有效',
    type        int default 1    null comment '配置类型',
    config_desc varchar(256)     null comment '描述'
)
    comment '系统配置' charset = utf8;

create index sys_config__index_key
    on sys_config (`key`);
```
## 定时任务使用


#### 例子
```java
//通知推送定时
    directBroadcastService.liveNotice(entity.getId(),
                KZEngine.generateKz(LocalDateTime.now(),
                        // 到达开播时间前30分钟执行通知推送
                        entity.getExpectStartTime().minusMinutes(30))
     );
    
    @KSchedule(beanName = "v3DirectBroadcastService", desc = "直播开始通知", dynamicDelay = true, maxRetry = 2, retryMillis = 10000)
    public Boolean liveNotice(String id, @KScheduleDelay String delayKz) {
        systemMsgService.sendSystemMsg(SystemMsgType.liveStart, id, null, null);
        return true;
    }

```

#### sql
```sql
-- auto-generated definition
create table sys_schedule
(
    id             varchar(64)      not null comment '主键'
        primary key,
    create_time    datetime         null comment '创建时间',
    modify_time    datetime         null comment '修改时间',
    deleted        bit default b'0' null comment '0 未删除/ 1 删除',
    delete_time    datetime         null comment '删除时间',
    create_by      varchar(64)      null comment '创建来源',
    update_by      varchar(64)      null comment '修改来源',
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
    comment '系统分布式任务';

create index index_status
    on sys_schedule (status);


```
