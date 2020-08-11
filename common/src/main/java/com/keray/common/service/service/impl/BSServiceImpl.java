package com.keray.common.service.service.impl;

import com.keray.common.IBSEntity;
import com.keray.common.service.service.BSService;

import java.io.Serializable;

/**
 * @author by keray
 * date:2019/7/25 16:03
 */
public abstract class BSServiceImpl<BS extends IBSEntity<BS, ID>, ID extends Serializable> extends BServiceImpl<BS> implements BSService<BS, ID> {

}
