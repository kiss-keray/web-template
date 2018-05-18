package com.nix.controller.admin;

import com.nix.common.ReturnObject;
import com.nix.common.annotation.AdminController;
import com.nix.util.ReturnUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kiss
 * @date 2018/05/05 20:02
 */
@RestController
@AdminController
@RequestMapping("/system")
public class SystemController {
    //上传文件的保存路径
    private final static String UPLOAD_IMAGE_PATH = "/file/images/";
    //上传文件接口
    @PostMapping("/upload")
    public Map upload(@RequestParam("file") MultipartFile file,
                      @RequestParam("fileType") String fileType) throws Exception {
        Map<String,Object> data = new HashMap<>();
        String fileName = file.getOriginalFilename();
        String url;
        switch (fileType) {
            case "image": {
                File image = new File(this.getClass().getResource("/").getFile() + UPLOAD_IMAGE_PATH + fileName);
                file.transferTo(image);
                url = UPLOAD_IMAGE_PATH + fileName;
                data.put("message", "操作成功");
                data.put("state", "SUCCESS");
            }break;
            default:return null;
        }
        data.put("url", url);
        return data;
    }
}
