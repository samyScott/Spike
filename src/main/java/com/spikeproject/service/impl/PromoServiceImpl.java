package com.spikeproject.service.impl;

import com.spikeproject.dao.PromoDOMapper;
import com.spikeproject.dataobject.PromoDO;
import com.spikeproject.error.BusinessException;
import com.spikeproject.error.EmBusinessError;
import com.spikeproject.model.ItemModel;
import com.spikeproject.model.PromoModel;
import com.spikeproject.model.UserModel;
import com.spikeproject.service.IItemService;
import com.spikeproject.service.IPromoService;
import com.spikeproject.service.IUserService;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author samy
 * @date 2020/1/5 21:22
 */
@Service
public class PromoServiceImpl implements IPromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Autowired
    private IItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IUserService userService;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        PromoModel promoModel = convertModelFromDO(promoDO);
        if (promoModel == null){
            return null;
        }

        setPromoModelStatus(promoModel);

        return promoModel;
    }

    @Override
    public void publishPromo(Integer promoId) {
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if (promoDO.getItemId() == null || promoDO.getItemId().intValue() == 0){
            return;
        }

        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());
        if (itemModel == null){
            return;
        }

        //将库存同步至redis内
        redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(),itemModel.getStock());
        //将大闸的限制数至redis内
        redisTemplate.opsForValue().set("promo_door_count_" + promoId,itemModel.getStock() * 3);
    }

    @Override
    public String generateSpikeToken(Integer promoId, Integer itemId, Integer userId) {
        //判断库存是否已售罄
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)){
            return null;
        }

        //校验商品是否在秒杀时间内
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);

        PromoModel promoModel = convertModelFromDO(promoDO);
        if (promoModel == null){
            return null;
        }

        setPromoModelStatus(promoModel);
        if (promoModel.getStatus() != 2 || promoModel.getItemId().intValue() != itemId){
            return null;
        }

        //判断商品是否存在
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null){
            return null;
        }

        //判断用户是否存在
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null){
            return null;
        }

        //获取活动count数量
        long result = redisTemplate.opsForValue().increment("promo_door_count" + promoId,-1);
        if (result < 0) {
            return null;
        }

        //生成令牌token
        String spikeToken = UUID.randomUUID().toString().replace("-","");
        String key = "promo_token_" + promoId + "_itemId_" + itemId + "_userId_" + userId;
        redisTemplate.opsForValue().set(key,spikeToken);
        redisTemplate.expire(key,10, TimeUnit.MINUTES);


        return spikeToken;
    }

    /**
     * <h2>转化promoDO为promoModel</h2>
     * @param promoDO {@link PromoDO}
     * @return {@link PromoModel}
     */
    private PromoModel convertModelFromDO(PromoDO promoDO){
        if (promoDO == null){
            return null;
        }

        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));

        return promoModel;
    }


    /**
     * <h2>根据时间设置PromoModel的活动状态</h2>
     * @param promoModel {@link PromoModel}
     */
    private void setPromoModelStatus(PromoModel promoModel){
        if (promoModel == null){
            return ;
        }

        //设置秒杀状态
        DateTime now = new DateTime();
        if (promoModel.getStartDate().isAfter(now)){
            promoModel.setStatus(1);
        }else if (promoModel.getEndDate().isBefore(now)){
            promoModel.setStatus(3);
        }else {
            promoModel.setStatus(2);
        }
    }
}
