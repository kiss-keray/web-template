package com.nix.dao;

import com.nix.dao.base.BaseMapper;
import com.nix.model.MemberBaseModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author 11723
 */
@Repository
public interface MemberMapper extends BaseMapper<MemberBaseModel> {
    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     * */
    MemberBaseModel login(@Param("username") String username, @Param("password") String password);
}