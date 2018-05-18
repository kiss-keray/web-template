package com.nix.controller.common;

import com.nix.common.annotation.AdminController;
import com.nix.common.annotation.Clear;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Kiss
 * @date 2018/05/01 20:23
 * 路由controller
 */
@Controller
public class RouterController {

    /**
     * 用户主页
     * */
    @GetMapping("/")
    public String userIndex() {
        return "/member/index.html";
    }


    /**
     * 用户登录界面
     * */
    @GetMapping("/member/login")
    public String userLogin() {
        return "/member/login.html";
    }

    /**
     * 管理员主页
     * */
    @GetMapping("/admin")
    public String adminIndex() {
        return "/admin/login/login.html";
    }
}
