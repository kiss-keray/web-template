package com.nix.service.impl;

import com.nix.Exception.WebException;
import com.nix.common.cache.MemberCache;
import com.nix.dao.MemberMapper;
import com.nix.model.MemberBaseModel;
import com.nix.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * @author Kiss
 * @date 2018/05/01 20:13
 * 用户service
 */
@Service
public class MemberService extends BaseService<MemberBaseModel> {
    public final static String ADMIN_USERNAME = "admin";
    public final static String MEMBER_IMG_PATH = "/images/member/";
    public final static String MEMBER_DEFAULT_IMG = "default.jpg";

    @Autowired
    private MemberMapper memberMapper;

    public MemberBaseModel login(String username, String password, HttpServletRequest request) {
        MemberBaseModel user = MemberCache.currentUser();
        if (user == null) {
            user = memberMapper.login(username,password);
            request.getSession(true).setAttribute(MemberCache.USER_SESSION_KEY,user);
        }
        return user;
    }

    public MemberBaseModel registered(MemberBaseModel user, HttpServletRequest request) throws Exception {
        add(user);
        user = findByUsername(user.getUsername());
        if (user != null) {
            request.getSession(true).setAttribute(MemberCache.USER_SESSION_KEY,user);
        }
        return user;
    }

    public MemberBaseModel findByUsername(String username) {
        return memberMapper.findByOneField("username",username).get(0);
    }

    @Override
    public MemberBaseModel add(MemberBaseModel model) throws Exception {
        if (ADMIN_USERNAME.equals(model.getUsername())) {
            throw new WebException(401,"不能使用admin做完用户名");
        }
        return super.add(model);
    }

}
