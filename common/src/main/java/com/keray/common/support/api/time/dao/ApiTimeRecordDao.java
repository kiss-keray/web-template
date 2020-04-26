package com.keray.common.support.api.time.dao;

import com.keray.common.support.api.time.model.ApiTimeRecordModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author by keray
 * date:2019/12/4 3:24 PM
 */
@NoRepositoryBean
public interface ApiTimeRecordDao<T extends ApiTimeRecordModel> extends MongoRepository<T, Long> {
}
