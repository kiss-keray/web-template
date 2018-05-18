package com.nix.dao;

import com.nix.dao.base.BaseMapper;
import com.nix.model.RoleBaseModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
/**
 * @author Kiss
 * @date 2018/05/03 17:51
 */
@Repository
public interface RoleMapper extends BaseMapper<RoleBaseModel> {
    /**
     * 添加 角色-接口 中间表添加
     * @param roleId
     * @param interfaceId
     * */
    void insertRoleMiddleInterface(@Param("roleId") Integer roleId, @Param("interfaceId") Integer interfaceId);
    /**
     * 删除
     * @param roleId
     * @param interfaceId
     * */
    void deleteRoleMiddleInterface(@Param("roleId") Integer roleId, @Param("interfaceId") Integer interfaceId);

}
