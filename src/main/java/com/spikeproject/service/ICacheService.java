package com.spikeproject.service;

/**
 * <h1>封装本地缓存操作类</h1>
 * @author samy
 * @date 2020/1/11 21:57
 */
public interface ICacheService {

    /**
     * <h2>向cache中存值/h2>
     * @param key
     * @param value
     */
    void setCommonCache(String key,Object value);

    /**
     * <h2>向cache中取值</h2>
     * @param key
     * @return
     */
    Object getFromCommonCache(String key);
}
