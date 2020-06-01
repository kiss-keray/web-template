package com.keray.common.service.model.base;

import com.keray.common.IBaseEntity;

/**
 * @author by keray
 * date:2019/9/4 16:07
 * 具有商户分组能力的model
 */
public interface AbstractMerchants extends IBaseEntity {

    String getMerchantsCode();

    void setMerchantsCode(String merchantsCode);
}
