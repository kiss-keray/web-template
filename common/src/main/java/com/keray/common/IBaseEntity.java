package com.keray.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * @author by keray
 * date:2019/10/14 14:00
 */
public interface IBaseEntity {

    String getId();

    void setId(String id);

    LocalDateTime getCreateTime();

    void setCreateTime(LocalDateTime createTime);

    LocalDateTime getModifyTime();

    void setModifyTime(LocalDateTime modifyTime);

    @JsonIgnore
    Boolean getDeleted();

    void setDeleted(Boolean deleted);

    @JsonIgnore
    LocalDateTime getDeleteTime();

    void setDeleteTime(LocalDateTime deleteTime);

    String getCreateBy();

    void setCreateBy(String createBy);

    @JsonIgnore
    String getModifyBy();

    void setModifyBy(String modifyBy);

    default IBaseEntity clearBaseField() {
        return this;
    }
}
