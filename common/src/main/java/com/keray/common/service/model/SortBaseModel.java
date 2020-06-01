package com.keray.common.service.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keray.common.BaseEntity;
import com.keray.common.service.model.base.SortModel;
import lombok.*;

/**
 * @author by keray
 * date:2020/3/9 12:43 PM
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("xxxx")
public
class SortBaseModel extends BaseEntity implements SortModel {
    Integer sort;
}
