package com.keray.common.service.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keray.common.*;
import com.keray.common.annotation.DiamondSupport;
import com.keray.common.exception.BizRuntimeException;
import com.keray.common.service.SystemConstants;
import com.keray.common.service.ienum.type.SysConfigType;
import com.keray.common.service.mapper.SysConfigMapper;
import com.keray.common.service.model.SysConfigModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author by keray
 * date:2019/8/26 10:29
 */
@Slf4j
public class SysConfigService implements BaseService<SysConfigModel> {
    @Resource
    private SysConfigMapper sysConfigMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private final Map<String, Field> diamondFields = new HashMap<>();

    @Override
    public IBaseMapper<SysConfigModel> getMapper() {
        return sysConfigMapper;
    }

    @Override
    public Boolean insert(SysConfigModel entity) {
        Assert.notEmpty(entity.getKey());
        try {
            notifyUpdate(entity);
        } catch (IllegalAccessException e) {
            throw new BizRuntimeException(CommonResultCode.dataChangeError);
        }
        SysConfigModel old = sysConfigMapper.selectOne(Wrappers.lambdaQuery(new SysConfigModel())
                .eq(SysConfigModel::getKey, entity.getKey()));
        if (old != null) {
            entity.setId(old.getId());
            return this.update(entity);
        }
        return getMapper().insert(entity) == 1;
    }

    @Override
    public Boolean update(SysConfigModel entity) {
        Assert.notEmpty(entity.getKey());
        all:
        if (StrUtil.isBlank(entity.getId())) {
            SysConfigModel old = sysConfigMapper.selectOne(Wrappers.lambdaQuery(new SysConfigModel())
                    .eq(SysConfigModel::getKey, entity.getKey()));
            if (old != null) {
                entity.setId(old.getId());
                break all;
            }
            return this.insert(entity);
        }
        try {
            notifyUpdate(entity);
        } catch (IllegalAccessException e) {
            throw new BizRuntimeException(CommonResultCode.dataChangeError);
        }
        return getMapper().updateById(entity) == 1;
    }

    public Object getValue(String key) {
        Field field = diamondFields.get(key);
        if (field != null) {
            try {
                return field.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return Fetch.fetch(
                sysConfigMapper.selectOne(Wrappers.lambdaQuery(new SysConfigModel())
                        .select(SysConfigModel::getValue)
                        .eq(SysConfigModel::getKey, key)
                        .eq(SysConfigModel::getStatus, 1)
                )).then(conf -> conf == null ? null : conf.getValue())
                .then((Function<String, Object>) JSON::parse)
                .catchFetch((e, d) -> {
                    if (e instanceof JSONException) {
                        return d;
                    }
                    return null;
                })
                .getData();
    }

    public void updateValue(String key, String value) {
        this.update(SysConfigModel.builder()
                .value(value)
                .key(key)
                .build());
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/8/29 16:17</h3>
     * 初始化diamond配置信息
     * </p>
     *
     * @param
     * @return <p> {@link } </p>
     * @throws
     */
    @PostConstruct
    public void initDiamond() {
        for (Field field : getFields(this.getClass(), new LinkedList<>())) {
            DiamondSupport diamondSupport = field.getAnnotation(DiamondSupport.class);
            if (diamondSupport != null) {
                log.info("diamond 扫描字段：key={},name={},type={}", diamondSupport.key(), diamondSupport.name(), field.getType().getSimpleName());
                diamondFields.put(diamondSupport.key(), field);
            }
        }
        this.loadConfig();
    }

    public List<Field> getFields(Class<?> clazz, List<Field> fields) {
        if (clazz == null) {
            return fields;
        }
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        return getFields(clazz.getSuperclass(), fields);
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/8/29 16:16</h3>
     * 获取diamond配置信息
     * </p>
     *
     * @param
     * @return <p> {@link List<Map<String,Object>>} </p>
     * @throws
     */
    public List<Map<String, Object>> getDiamondConfig() {
        return diamondFields.keySet().stream().parallel().map(key -> {
            try {
                return MapUtil.<String, Object>builder()
                        .put("key", key)
                        .put("name", diamondFields.get(key).getAnnotation(DiamondSupport.class).name())
                        .put("value", diamondFields.get(key).get(this))
                        .build();
            } catch (IllegalAccessException e) {
                log.error("添加diamond配置字段异常 key={},e={}", key, e);
            }
            return null;
        }).collect(Collectors.toList());
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/8/29 15:39</h3>
     * 自动同步diamond配置，每天凌晨3点执行 容错处理
     * </p>
     *
     * @param
     * @return <p> {@link } </p>
     * @throws
     */
    @Scheduled(cron = "0 0 3 * * ? ")
    public void loadConfig() {
        List<SysConfigModel> initConfig = sysConfigMapper.selectList(
                Wrappers.lambdaQuery(new SysConfigModel())
                        .select(SysConfigModel::getKey,
                                SysConfigModel::getType,
                                SysConfigModel::getValue)
                        .eq(SysConfigModel::getType, SysConfigType.diamond)
                        .eq(SysConfigModel::getStatus, 1)
        );
        for (SysConfigModel configModel : initConfig) {
            try {
                updateConfig(configModel);
            } catch (IllegalAccessException e) {
                log.error("diamond值设置失败：" + configModel, e);
            }
        }
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/20 11:33</h3>
     * mq通知，在分布式下实时修改配置
     * </p>
     *
     * @param configModel
     * @return <p> {@link } </p>
     * @throws
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue,
            exchange = @Exchange(value = SystemConstants.DIAMOND_MQ_EXCHANGE,
                    type = ExchangeTypes.TOPIC),
            key = SystemConstants.DIAMOND_MQ_ROUTER_KEY)})
    public void mqUpdate(SysConfigModel configModel) {
        // 直接成功 避免逻辑错误导致消息堆积
        SysThreadPool.execute(() -> {
            log.info("mq config update:{}", configModel);
            try {
                updateConfig(configModel);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/20 11:34</h3>
     * diamond修改发送mq通知
     * </p>
     *
     * @param config
     * @return <p> {@link } </p>
     * @throws
     */
    private void notifyUpdate(SysConfigModel config) throws IllegalAccessException {
        if (config.getType() != SysConfigType.diamond) {
            return;
        }
        updateConfig(config);
        SysThreadPool.execute(() -> {
            log.info("diamond config update mq notify:{}", config);
            rabbitTemplate.convertAndSend(SystemConstants.DIAMOND_MQ_EXCHANGE, SystemConstants.DIAMOND_MQ_ROUTER_KEY, config);
        });
    }

    private void updateConfig(SysConfigModel config) throws IllegalAccessException {
        if (config.getType() == SysConfigType.diamond && diamondFields.containsKey(config.getKey())) {
            log.info("diamond值修改:key={},value={}", config.getKey(), config.getValue());
            Field field = diamondFields.get(config.getKey());
            try {
                if (ClassUtil.isSimpleValueType(field.getType())) {
                    // 设置field值
                    field.set(this, Convert.convert(field.getType(), config.getValue()));
                } else {
                    // Convert.convert转换失败 字符串转基本数据类型不会失败，转对象类型时使用json反序列
                    try {
                        field.set(this, JSON.parseObject(config.getValue(), field.getType()));
                    } catch (IllegalAccessException ex) {
                        log.error("diamond JSON值设置失败:key={},value={}", config.getKey(), config.getValue());
                        throw ex;
                    }
                }
            } catch (Exception e) {
                log.error("diamond设置值失败:key={},value={}", config.getKey(), config.getValue());
            }
        }
    }
}
