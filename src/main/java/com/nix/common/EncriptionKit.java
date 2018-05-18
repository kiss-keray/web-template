package com.nix.common;

/**
 * Created by 11723 on 2017/7/29.
 */

import java.security.MessageDigest;

import com.alibaba.druid.filter.config.ConfigTools;

/**
 *         加密工具
 */
public class EncriptionKit {


    /**
     * password解密
     *
     */
    public static String passwordDecrypt(String publicKey, String password) {
        String result = null;
        try {
            // 解密
            result = ConfigTools.decrypt(publicKey, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * md5加密
     *
     * @param srcStr input string
     * @return output encription string
     */
    public static final String encrypt(String srcStr) {
        try {
            String result = "";
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(srcStr.getBytes("utf-8"));
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF).toUpperCase();
                result += ((hex.length() == 1) ? "0" : "") + hex;
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
