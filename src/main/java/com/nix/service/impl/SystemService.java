package com.nix.service.impl;

import com.nix.common.EncriptionKit;
import com.nix.common.cache.MemberCache;
import com.nix.common.captcha.CaptchaRender;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Kiss
 * @date 2018/05/02 10:50
 */
@Service
public class SystemService {
    public final static String CAPTCHA_NAME = "captcha";

    /**
     * 获取图片验证码
     * */
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response){
        int width = 0, height = 0, minnum = 0, maxnum = 0, fontsize = 0;
        CaptchaRender captcha = new CaptchaRender(request,response);
        if (fontsize > 0) {
            captcha.setFontSize(fontsize, fontsize);
        }
        // 干扰线数量 默认0
        captcha.setLineNum(2);
        // 噪点数量 默认50
        captcha.setArtifactNum(30);
        // 使用字符 去掉0和o 避免难以确认
        captcha.setCode("123456789");
        //验证码在session里的名字 默认 captcha,创建时间为：名字_time
        // captcha.setCaptchaName("captcha");
        //验证码颜色 默认黑色
        // captcha.setDrawColor(new Color(255,0,0));
        //背景干扰物颜色  默认灰
        // captcha.setDrawBgColor(new Color(0,0,0));
        //背景色+透明度 前三位数字是rgb色，第四个数字是透明度  默认透明
        // captcha.setBgColor(new Color(225, 225, 0, 100));
        //滤镜特效 默认随机特效 //曲面Curves //大理石纹Marble //弯折Double //颤动Wobble //扩散Diffuse
        captcha.setFilter(CaptchaRender.FilterFactory.Curves);
        // 随机色 默认黑验证码 灰背景元素
        captcha.setRandomColor(true);
        captcha.render();
    }

    /**
     * 验证验证码
     *
     * @param captchaToken
     *            token
     * @return boolean
     */
    public  boolean doCaptcha(String captchaToken) {
        HttpSession session = MemberCache.getSession();
        if (session.getAttribute(CAPTCHA_NAME) != null) {
            String captcha = session.getAttribute(CAPTCHA_NAME).toString();
            if (captchaToken != null && captcha.equalsIgnoreCase(EncriptionKit.encrypt(captchaToken))) {
                return true;
            }
        }
        return false;
    }
}
