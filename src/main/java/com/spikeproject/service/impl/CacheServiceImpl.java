package com.spikeproject.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.spikeproject.service.ICacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author samy
 * @date 2020/1/11 21:59
 */
@Service
public class CacheServiceImpl implements ICacheService {

    private Cache<String,Object> commonCache = null;

    @PostConstruct
    public void init(){
        commonCache = CacheBuilder.newBuilder()
                //设置缓存容量的初始大小
                .initialCapacity(10)
                //设置缓存中最大存储100个key，超过100之后会按LRU的策略移除缓存
                .maximumSize(100)
                //被写入后1分钟时间过期
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key,value);
    }

    @Override
    public Object getFromCommonCache(String key) {
        return commonCache.getIfPresent(key);
    }
}
