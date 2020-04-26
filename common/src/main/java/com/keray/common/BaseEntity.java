package com.keray.common;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author by keray
 * date:2019/7/25 15:33
 */
@Data
public class BaseEntity extends Model<BaseEntity> implements IBaseEntity {
    /**
     * 主键id
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime modifyTime;

    /**
     * 是否删除
     */
    @TableLogic(delval = "1", value = "0")
    private Boolean deleted = false;

    /**
     * 删除时间
     */
    private LocalDateTime deleteTime;

    /**
     * 创建来源
     */
    private String createBy;

    /**
     * 修改来源
     */
    private String modifyBy;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else {
            return obj instanceof BaseEntity && StrUtil.equals(this.getId(), ((BaseEntity) obj).getId());
        }
    }

    public BaseEntity clearBaseField() {
        this.setDeleted(null);
        this.setDeleteTime(null);
        this.setModifyTime(null);
        this.setModifyBy(null);
        this.setCreateBy(null);
        this.setCreateTime(null);
        return this;
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/4 15:00</h3>
     * 清除实体中的空字符串
     * </p>
     *
     * @param
     * @return <p> {@link T} </p>
     * @throws
     */
    public <T> T clearEmptyStringField(Class<T> clazz) {
        List<Field> fields = scanFields(this.getClass(), null);
        for (Field field : fields) {
            try {
                if (field.getType() == String.class) {
                    Method get = scanMethod(clazz, "get" + StrUtil.upperFirst(field.getName()));
                    if (get == null) {
                        continue;
                    }
                    String result = (String) get.invoke(this);
                    if ("".equals(result)) {
                        Method set = scanMethod(clazz, "set" + StrUtil.upperFirst(field.getName()), String.class);
                        set.invoke(this, (Object) null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (T) this;
    }

    private Method scanMethod(Class<?> p, String name, Class<?>... type) throws NoSuchMethodException {
        if (p == null) {
            return null;
        }
        Method method;
        try {
            method = p.getMethod(name, type);
        } catch (NoSuchMethodException e) {
            method = null;
        }
        return method == null ? scanMethod(p.getSuperclass(), name) : method;
    }

    private List<Field> scanFields(Class<?> p, List<Field> fields) {
        if (fields == null) {
            fields = new LinkedList<>();
        }
        fields.addAll(Arrays.asList(p.getDeclaredFields()));
        if (p.getSuperclass() != null) {
            scanFields(p.getSuperclass(), fields);
        }
        return fields;
    }
}
