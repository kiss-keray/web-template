package com.keray.common.utils;


import cn.hutool.crypto.digest.MD5;

public final class DigesterUtil {

    private static final MD5 MD_5 = new MD5();

    public static String MD5Encode(String origin, String charsetname) {
        return MD_5.digestHex(origin,charsetname);
    }

    public static String MD5Encode(String origin) {
        return MD5Encode(origin,"UTF-8");
    }


    public static String getMessageDigest(byte[] buffer) {
        return MD_5.digestHex(buffer);
    }

}
