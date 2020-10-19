package com.keray.aliyun.ai;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.imageseg.model.v20191230.*;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import com.keray.aliyun.AliyunConfig;
import com.keray.aliyun.AliyunPlugins;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author by keray
 * date:2020/9/24 9:56 下午
 */
@Service
@Slf4j
@ConditionalOnBean(AliyunConfig.class)
public class ImagesegPlugins extends AliyunPlugins {

    private IAcsClient acsClient;

    @PostConstruct
    private void init() {
    }

    private synchronized IAcsClient createClient() {
        if (acsClient == null) {
            DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
            acsClient = new DefaultAcsClient(profile);
        }
        return acsClient;
    }

    public String imageseg(String url) {
        SegmentLogoRequest request = new SegmentLogoRequest();
        request.setRegionId("cn-shanghai");
        request.setImageURL(url);
        try {
            SegmentLogoResponse response = createClient().getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
            return response.getData().getImageURL();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
        return null;
    }


    public String imageseg1(String url) {
        SegmentCommonImageRequest request = new SegmentCommonImageRequest();
        request.setRegionId("cn-shanghai");
        request.setImageURL(url);
        try {
            SegmentCommonImageResponse response = createClient().getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
            return response.getData().getImageURL();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
        return null;
    }
}
