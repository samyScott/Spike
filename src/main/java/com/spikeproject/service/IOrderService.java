package com.spikeproject.service;

import com.spikeproject.error.BusinessException;
import com.spikeproject.model.OrderModel;

/**
 * <h1>订单接口服务定义</h1>
 * @author samy
 * @date 2020/1/4 21:51
 */
public interface IOrderService {

    /**
     * <h2>创建订单</h2>
     * @param userId 用户id
     * @param itemId 商品id
     * @param promoId 秒杀id
     * @param amount 商品数量
     * @return {@link OrderModel}
     */
    OrderModel createOrder(Integer userId,Integer itemId,Integer promoId,Integer amount,String stockLogId) throws BusinessException;

    /**
     * <h2>生成订单编号</h2>
     */
    String generateOrderNo();
}
