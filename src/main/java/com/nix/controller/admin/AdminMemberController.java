package com.nix.controller.admin;

import com.nix.common.Pageable;
import com.nix.common.ReturnObject;
import com.nix.common.annotation.AdminController;
import com.nix.common.cache.MemberCache;
import com.nix.model.MemberBaseModel;
import com.nix.service.impl.MemberService;
import com.nix.service.impl.RoleService;
import com.nix.util.ReturnUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kiss
 * @date 2018/05/01 23:53
 */
@RestController
@RequestMapping("/admin/member")
@AdminController
public class AdminMemberController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private RoleService roleService;


    /**
     * 批量删除member
     * @param ids ids
     * */
    @PostMapping("/delete")
    public ReturnObject delete(@RequestParam("ids") Integer[] ids) throws Exception {
        memberService.delete(ids);
        return ReturnUtil.success();
    }


    /**
     * 用户名校验 判断用户名是否被占用
     * @return true 未被占用
     * */
    @GetMapping("/checkUsername")
    public Boolean checkUsername(String username) {
        return memberService.findByUsername(username) == null;
    }

    /**
     * 查看用户的全部信息
     * */
    @GetMapping("/view")
    public ReturnObject select(@RequestParam("id") Integer id) {
        return ReturnUtil.success(memberService.findById(id));
    }

    /**
     * 查看用户列表
     * */
    @PostMapping("/list")
    public ReturnObject list(@ModelAttribute Pageable<MemberBaseModel> pageable) throws Exception {
        Map additionalData = new HashMap();
        List list = pageable.getList(memberService);
        additionalData.put("total",pageable.getCount());
        return ReturnUtil.success(null,list,additionalData);
    }
}
