package com.keray.aliyun.oss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.StorageClass;
import com.keray.aliyun.AliyunPlugins;
import com.keray.common.utils.TimeUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author by keray
 * date:2020/9/19 9:50 上午
 */
@Service
@Slf4j
@ConditionalOnBean(OssConfig.class)
public class OssPlugins extends AliyunPlugins {

    private final OssConfig ossConfig;

    private OSS ossClient;

    private final AtomicInteger threadCount = new AtomicInteger(0);

    private final ThreadPoolExecutor executor;

    public OssPlugins(OssConfig ossConfig) {
        this.ossConfig = ossConfig;
        executor = new ThreadPoolExecutor(ossConfig.getPollCount(), ossConfig.getPollMax(),
                1, TimeUnit.MINUTES, new LinkedBlockingDeque<>(), r -> {
            Thread t = new Thread(r);
            t.setName("oss-" + threadCount.getAndIncrement());
            return t;
        });
    }

    private synchronized OSS createClient() {
        if (ossClient == null) {
            ossClient = new OSSClientBuilder().build(ossConfig.getEndpoint(), aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
        }
        return ossClient;
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2020/9/19 10:04 上午</h3>
     * </p>
     *
     * @param inputStream 输入流
     * @return <p> {@link } </p>
     * @throws
     */
    public String streamUploadSync(InputStream inputStream, String fileName) {
        String filePath = getFilePath(fileName);
        PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), filePath, inputStream);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
        metadata.setObjectAcl(CannedAccessControlList.PublicRead);
        putObjectRequest.setMetadata(metadata);
        createClient().putObject(putObjectRequest);
        return filePath;
    }

    public Future<String> streamUploadASync(InputStream inputStream, String fileName) {
        return executor.submit(() -> streamUploadSync(inputStream, fileName));
    }

    @SneakyThrows
    public String[] streamUploadSync(MultiInputUpload... uploads) {
        return uploadSync(uploads, (obj) -> streamUploadSync(obj.getInputStream(), obj.getFileName()));
    }

    public Future<String[]> streamUploadASync(MultiInputUpload... uploads) {
        return executor.submit(() -> streamUploadSync(uploads));
    }


    @SneakyThrows
    public String webFileUploadSync(String url) {
        InputStream inputStream = HttpUtil.createGet(url).execute().bodyStream();
        return streamUploadSync(inputStream, getWebFileName(url));
    }

    public Future<String> webFileUploadASync(String url) {
        return executor.submit(() -> webFileUploadSync(url));
    }


    public String[] webFileUploadSync(String... urls) {
        return uploadSync(urls, this::webFileUploadSync);
    }

    public Future<String[]> webFileUploadASync(String... urls) {
        return executor.submit(() -> webFileUploadSync(urls));
    }


    @SneakyThrows
    private <T extends Object> String[] uploadSync(T[] data, Function<T, String> function) {
        class r {
            final int index;
            final String result;

            r(int i, String r) {
                index = i;
                result = r;
            }
        }
        ExecutorCompletionService<r> completionService = new ExecutorCompletionService<>(executor);
        for (int i = 0; i < data.length; i++) {
            int finalI = i;
            completionService.submit(() -> {
                String result = function.apply(data[finalI]);
                return new r(finalI, result);
            });
        }
        String[] result = new String[data.length];
        Future<r> future;
        for (int i = 0; ; i++) {
            if (i >= data.length) {
                break;
            }
            try {
                future = completionService.take();
                r r = future.get();
                result[r.index] = r.result;
            } catch (Exception e) {
                log.error("批量上传异常", e);
            }
        }
        return result;
    }

    private String getFilePath(String fileName) {
        return StrUtil.format("{}/{}/{}", ossConfig.getBasePath(), TimeUtil.DATE_TIME_FORMATTER_DAY_YMD.format(LocalDate.now()), System.nanoTime() + fileName);
    }

    private String getWebFileName(String url) {
        if (StrUtil.isBlank(url) || !url.startsWith("http")) {
            throw new IllegalStateException("webFile mast start with http:" + url);
        }
        String[] str = url.split("/");
        return str[str.length - 1];
    }
}
