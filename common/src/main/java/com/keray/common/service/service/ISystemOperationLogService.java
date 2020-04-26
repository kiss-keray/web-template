package com.keray.common.service.service;

import com.keray.common.BaseService;
import com.keray.common.service.ienum.SystemOperationAction;
import com.keray.common.service.model.SystemOperationLogModel;

/**
 * @author by keray
 * date:2019/8/6 14:30
 * 系统操作记录
 */
public interface ISystemOperationLogService extends BaseService<SystemOperationLogModel> {

    /**
     * 操作记录
     *
     * @param tableName
     * @param operationName
     * @param action
     * @param remark
     */
    void log(String tableName, String operationName, SystemOperationAction action, String remark);

}
