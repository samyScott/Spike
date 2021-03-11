package com.spikeproject.service;

import com.spikeproject.model.PromoModel;

/**
 * <h1>秒杀接口服务定义</h1>
 * @author samy
 * @date 2020/1/5 21:19
 */
public interface IPromoService {

    /**
     * <h2>获取即将开始或正在进行的商品秒杀信息</h2>
     * @param itemId 商品id
     * @return {@link PromoModel}
     */
    PromoModel getPromoByItemId(Integer itemId);

    /**
     * <h2>根据活动id将对应商品库存放入redis缓存中</h2>
     * @param promoId
     */
    void publishPromo(Integer promoId);


    /**
     * <h2>生成秒杀令牌并校验</h2>
     * @param promoId 活动id
     * @param itemId 商品id
     * @param userId 用户id
     * @return 令牌token
     */
    String generateSpikeToken(Integer promoId,Integer itemId,Integer userId);


}
