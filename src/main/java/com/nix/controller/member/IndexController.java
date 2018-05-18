package com.nix.controller.member;

import com.nix.common.ReturnObject;
import com.nix.common.annotation.Clear;
import com.nix.common.annotation.MemberController;
import com.nix.common.cache.MemberCache;
import com.nix.model.MemberBaseModel;
import com.nix.service.impl.MemberService;
import com.nix.service.impl.RoleService;
import com.nix.util.ReturnUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Kiss
 * @date 2018/04/25 11:30
 */
@RestController
@RequestMapping("/member")
@MemberController
public class IndexController {
    @Autowired
    private MemberService userService;
    @Autowired
    private RoleService roleService;

    @Clear
    @PostMapping("/registered")
    public ReturnObject registered(@ModelAttribute MemberBaseModel user, HttpServletRequest request) throws Exception {
        user.setRole(roleService.findByOneField("value","user").get(0));
        MemberBaseModel insertUser = userService.registered(user,request);
        if (insertUser == null) {
            return ReturnUtil.fail(user);
        }
        return ReturnUtil.success(insertUser);
    }
    /**
     * 用户修改自己的资料
     * */
    @PostMapping("/update")
    public ReturnObject updateUser(@ModelAttribute MemberBaseModel member, HttpServletRequest request) throws Exception {
        Assert.isTrue(MemberCache.currentUser().getUsername().equals(member.getUsername()),"非法修改");
        member = userService.update(member);
        request.getSession().setAttribute(MemberCache.USER_SESSION_KEY,member);
        return ReturnUtil.success(member);
    }

}
