package com.keray.common;

import java.io.Serializable;

/**
 * @author by keray
 * date:2020/7/15 9:39 上午
 */
public interface IBSEntity<BS extends IBSEntity<BS, ID>, ID extends Serializable> extends IBEntity<BS> {
    ID getId();

    void setId(ID id);
}
