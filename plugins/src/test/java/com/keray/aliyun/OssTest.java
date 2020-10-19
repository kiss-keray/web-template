package com.keray.aliyun;

import com.alibaba.fastjson.JSON;
import com.keray.aliyun.ai.ImagesegPlugins;
import com.keray.aliyun.oss.OssPlugins;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;

/**
 * @author by keray
 * date:2020/9/19 10:45 上午
 * {
 *     "url": "http://www.jiuhuar.com/craftbeer/599d4d407901122e1c3d4a35.html",
 *     "name": "啤酒工厂 酒鬼 皮尔森",
 *     "enName": "Bierfabrik Schluckspecht Pils",
 *     "n_产地": "德国",
 *     "n_风格": "拉格啤酒 皮尔森",
 *     "n_酒精度": "5.0%",
 *     "n_总体评分": "N/A",
 *     "n_评论数": "0 条评论",
 *     "n_色卡": "3",
 *     "n_想喝": "0",
 *     "n_喝过": "0",
 *     "n_苦度": "N/A",
 *     "desc": "这款啤酒是与柏林出版社合作的产品。酒标上题有来自柏林出版社出版的现代抒情诗人、作家Mikael Vogel的抒情诗《酒鬼》(An den Schluckspecht)，译文如下： 别人并没有邀请你，你却突然到了这里 笨手笨脚地站着，勉强地挺直了摇晃的身体 小心翼翼地轻敲着每个酒吧的门 眼神游离，红着脸说着模糊不清的话 整个世界都倾斜了 冷汗滋润着你弯曲的后背 你拿着那深度磨损的玻璃杯 嘴角还滴着酒 生活的艰辛使你清醒起来 干枯的生活使你慢慢失望，雨水也许会让你好些 肝脏已经被你自己光荣地毁掉 尿尿已是家常便饭，甚至不顾形象地呕吐 他是所有怪人里最徒劳的 但一觉醒来 这一切将会重来",
 *     "tags": [],
 *     "pic": "head_bg-FgT2X-mC1dnf_LilWUoei-nNSV1v.png",
 *     "picUrls": {
 *         "head_bg-FgT2X-mC1dnf_LilWUoei-nNSV1v.png": "https://file1.jiuhuar.com/bg-FgT2X-mC1dnf_LilWUoei-nNSV1v"
 *     },
 *     "comments": []
 * }
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationTest.class)
@Slf4j
public class OssTest {

    @Resource
    private OssPlugins ossPlugins;

    @Resource
    private ImagesegPlugins imagesegPlugins;

    @Test
    public void ossUploadTest() throws Exception {
        FileInputStream inputStream = new FileInputStream("/Users/keray/Desktop/qrious.min.js");
        System.out.println(ossPlugins.streamUploadSync(inputStream, "qrious.min.js"));
    }

    @Test
    public void httpFileUploadTest() {
        System.out.println(ossPlugins.webFileUploadSync("https://file1.jiuhuar.com/bg-FgT2X-mC1dnf_LilWUoei-nNSV1v"));
    }

    @Test
    public void jsonRead() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("/Users/keray/Desktop/out.json"));
        String json;
        int i = 0;
        while ((json = reader.readLine()) != null) {
            try {
                JSON.parse(json);
                i++;
            }catch (Exception e) {
                log.error(json);
            }
        }
        System.out.println(i);
    }


    @Test
    public void imagesegTest() {
        System.out.println(imagesegPlugins.imageseg("https://xpj-sh.oss-cn-shanghai.aliyuncs.com/invi_LogoSegmenter_016009579454491000723_yyt0m7.png"));
    }

    @Test
    public void imagesegTest1() {
        System.out.println(imagesegPlugins.imageseg1("https://xpj-sh.oss-cn-shanghai.aliyuncs.com/invi_LogoSegmenter_016009579454491000723_yyt0m7.png"));
    }

}
