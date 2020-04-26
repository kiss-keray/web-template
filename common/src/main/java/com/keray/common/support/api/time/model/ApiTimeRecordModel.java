package com.keray.common.support.api.time.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author by keray
 * date:2019/12/4 3:19 PM
 */
@Document(collection = "api_time_record")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiTimeRecordModel implements Serializable {
    @Id
    private String id;

    /**
     * api名称
     */
    private String title;

    /**
     * 方法路径
     */
    @Indexed
    private String methodPath;

    /**
     * 设定最大时间 毫秒
     */
    private Integer gt;

    /**
     * api执行时间 毫秒
     */
    @Indexed
    private Integer execTime;

    private String time;

    private String url;


}
