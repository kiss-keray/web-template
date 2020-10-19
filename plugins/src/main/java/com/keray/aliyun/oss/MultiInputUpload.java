package com.keray.aliyun.oss;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;

/**
 * @author by keray
 * date:2020/9/19 10:43 上午
 */
@Data
@AllArgsConstructor
public class MultiInputUpload {
    private InputStream inputStream;
    private String fileName;
}
