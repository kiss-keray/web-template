package com.nix.controller.admin;

import com.nix.common.Pageable;
import com.nix.common.ReturnObject;
import com.nix.common.annotation.AdminController;
import com.nix.dto.RoleRoleInterfaceDto;
import com.nix.model.RoleBaseModel;
import com.nix.model.RoleInterfaceModel;
import com.nix.service.impl.RoleInterfaceService;
import com.nix.service.impl.RoleService;
import com.nix.util.ReturnUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kiss
 * @date 2018/05/03 11:42
 */
@RestController
@AdminController
@RequestMapping("/admin/role")
public class AdminRoleController {
    @Autowired
    private RoleService roleService;
    @Autowired
    private RoleInterfaceService roleInterfaceService;

    /**
     * 校验角色名是否被占用
     * @return true 未被占用
     * */
    @GetMapping("/checkName")
    public boolean checkName(@RequestParam("name") String name) {
        return roleService.findByName(name) == null;
    }

    /**
     * 添加一个角色
     * @param roleModel 角色的基本信息
     * @param roleInterfaceId 角色拥有权限的接口id数组
     * */
    @PostMapping("/add")
    public ReturnObject add(
            @ModelAttribute RoleBaseModel roleModel,
            @RequestParam(value = "roleInterfaceId",required = false) Integer[] roleInterfaceId) throws Exception {
        roleService.createRoleModelByInterfacesId(roleModel,roleInterfaceId);
        return ReturnUtil.success(roleService.add(roleModel));
    }

    @PostMapping("/delete")
    public ReturnObject delete(@RequestParam("ids") Integer[] ids) throws Exception {
        roleService.delete(ids);
        return ReturnUtil.success();
    }

    /**
     * 角色获取他拥护的授权的接口
     * @param id 角色的id
     * */
    @GetMapping("/interfaces")
    public ReturnObject getRoleInterfacesInAll(@RequestParam("id") Integer id) {
        //获取所有的接口列表
        List<RoleInterfaceModel> roleInterfaceModels = roleInterfaceService.list(null,null,"`group`","asc",null);
        RoleBaseModel roleModel = roleService.findById(id);
        List<RoleRoleInterfaceDto> list = new ArrayList<>();
        for (RoleInterfaceModel roleInterfaceModel:roleInterfaceModels) {
            RoleRoleInterfaceDto dto = new RoleRoleInterfaceDto();
            dto.setRoleInterface(roleInterfaceModel);
            dto.setHave(roleService.roleHaveTheInterface(roleModel,roleInterfaceModel));
            list.add(dto);
        }
        return ReturnUtil.success(list);
    }

    @GetMapping("/view")
    public ReturnObject select(@RequestParam("id") Integer id) {
        return ReturnUtil.success(roleService.findById(id));
    }
    @PostMapping("/update")
    public ReturnObject update(
            @ModelAttribute RoleBaseModel roleModel,
            @RequestParam(value = "roleInterfaceId",required = false) Integer[] roleInterfaceId) throws Exception {
        roleService.createRoleModelByInterfacesId(roleModel,roleInterfaceId);
        return ReturnUtil.success(roleService.update(roleModel));
    }


    @PostMapping("/list")
    public ReturnObject list(@ModelAttribute Pageable<RoleBaseModel> pageable) throws Exception {
        Map additionalData = new HashMap();
        List list = pageable.getList(roleService);
        additionalData.put("total",pageable.getCount());
        return ReturnUtil.success(null,list,additionalData);
    }
}
