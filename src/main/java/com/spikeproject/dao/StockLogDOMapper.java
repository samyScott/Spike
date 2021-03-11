package com.spikeproject.dao;

import com.spikeproject.dataobject.StockLogDO;

/**
 * @author samy
 * @date 2020/1/17 21:23
 */
public interface StockLogDOMapper {

    /**
     * <h2>查询流水详细信息</h2>
     * @param stockLogId 流水id
     * @return {@link StockLogDO}
     */
    StockLogDO selectByPrimaryKey(String stockLogId);

    /**
     * <h2>添加流水信息</h2>
     * @param stockLogDO {@link StockLogDO}
     * @return 更新的行数
     */
    int insert(StockLogDO stockLogDO);

    /**
     * <h2>通过流水id更新流水信息</h2>
     * @param stockLogDO {@link StockLogDO}
     * @return 更新的行数
     */
    int updateByPrimaryKeySelective(StockLogDO stockLogDO);
}
