package com.keray.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keray.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author by keray
 * date:2020/6/26 10:11 上午
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("plus")
public class PlusModel extends BaseEntity {

    private String name;

}
