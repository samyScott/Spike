package com.spikeproject.service;

import com.spikeproject.error.BusinessException;
import com.spikeproject.model.ItemModel;

import java.util.List;

/**
 * <h1>商品接口服务定义</h1>
 * @author samy
 * @date 2020/1/3 15:57
 */
public interface IItemService {

    /**
     * <h2>创建商品</h2>
     * @param itemModel {@link ItemModel}
     * @return {@link ItemModel}
     */
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    /**
     * <h2>商品列表浏览</h2>
     * @return {@link ItemModel}
     */
    List<ItemModel> listItem();

    /**
     * <h2>根据商品id获取商品详细信息</h2>
     * @param id 商品id
     * @return {@link ItemModel}
     */
    ItemModel getItemById(Integer id);

    /**
     * <h2>根据商品id获取redis缓存的商品详细信息</h2>
     * @param id 商品id
     * @return {@link ItemModel}
     */
    ItemModel getItemByIdInCache(Integer id);


//    /**
//     * <h2>异步商品减库存</h2>
//     * @param itemId 商品id
//     * @param amount 购买数量
//     * @return true/false
//     */
//    boolean asyncDecreaseStock(Integer itemId,Integer amount);

    /**
     * <h2>商品减库存</h2>
     * @param itemId 商品id
     * @param amount 购买数量
     * @return true/false
     */
    boolean decreaseStockInCache(Integer itemId,Integer amount);

//    /**
//     * <h2>商品回补库存</h2>
//     * @param itemId 商品id
//     * @param amount 购买数量
//     * @return true/false
//     */
//    boolean increaseStockInCache(Integer itemId,Integer amount);

    /**
     * <h2>商品销量增加</h2>
     * @param itemId 商品id
     * @param amount 购买数量
     */
    void increaseSales(Integer itemId,Integer amount);

    /**
     * <h2>初始化库存流水</h2>
     * @param itemId 商品id
     * @param amount 购买数量
     */
    String initStockLog(Integer itemId,Integer amount);
}
