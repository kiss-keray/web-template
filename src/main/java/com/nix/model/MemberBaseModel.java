package com.nix.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nix.model.base.BaseModel;
import com.nix.service.impl.MemberService;

import java.math.BigDecimal;

/**
 * @author 11723
 * 用户
 */
public class MemberBaseModel extends BaseModel<MemberBaseModel> {
    private String username;
    private String password;
    private Integer age;
    private Boolean sex;
    private String name;
    //电话
    private String phone;
    //账户余额
    private BigDecimal balance;
    //头像
    private String img;

    //用户角色
    private RoleBaseModel role;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setSex(Boolean sex) {
        this.sex = sex;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getImg() {
        return img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImg(String img) {
        this.img = img;
    }
    public RoleBaseModel getRole() {
        return role;
    }

    public void setRole(RoleBaseModel role) {
        this.role = role;
    }

    public Boolean getSex() {
        return sex;
    }

    public boolean isSuperAdmin() {
        return MemberService.ADMIN_USERNAME.equals(username);
    }

    @Override
    public String toString() {
        return "MemberBaseModel{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", age=" + age +
                ", sex=" + sex +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", balance=" + balance +
                ", img='" + img + '\'' +
                ", role=" + role +
                '}';
    }

    @JsonIgnore
    public String getRoleValue() {
        return getRole().getValue();
    }
}