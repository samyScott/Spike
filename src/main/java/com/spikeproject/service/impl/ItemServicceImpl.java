package com.spikeproject.service.impl;

import com.spikeproject.dao.ItemDOMapper;
import com.spikeproject.dao.ItemStockDOMapper;
import com.spikeproject.dao.StockLogDOMapper;
import com.spikeproject.dataobject.ItemDO;
import com.spikeproject.dataobject.ItemStockDO;
import com.spikeproject.dataobject.StockLogDO;
import com.spikeproject.error.EmBusinessError;
import com.spikeproject.error.BusinessException;
import com.spikeproject.model.ItemModel;
import com.spikeproject.model.PromoModel;
import com.spikeproject.service.IItemService;
import com.spikeproject.service.IPromoService;
import com.spikeproject.validator.ValidationResult;
import com.spikeproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author samy
 * @date 2020/1/3 16:01
 */
@Service
public class ItemServicceImpl implements IItemService {

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private IPromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        if (itemModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //校验入参
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }

        //转化itemmodel -> dataobject,写入数据库
        ItemDO itemDO = convertItemDOFromModel(itemModel);
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());

        ItemStockDO itemStockDO = convertItemStockDOFromModel(itemModel);
        itemStockDOMapper.insert(itemStockDO);

        //返回创建的对象
        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();

        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = convertModelFromDataObject(itemDO, itemStockDO);

            return itemModel;
        }).collect(Collectors.toList());

        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) return null;

        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        if (itemStockDO == null) return null;

        ItemModel itemModel = convertModelFromDataObject(itemDO, itemStockDO);
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus() != 3){
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_" + id);
        if (itemModel == null){
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set("item_validate_" + id,itemModel);
            redisTemplate.expire("item_validate_" + id,10, TimeUnit.MINUTES);
        }

        return itemModel;
    }


    @Override
    @Transactional
    public boolean decreaseStockInCache(Integer itemId, Integer amount) {
        Long result = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount * -1);
        if (result > 0){
            return true;
        }else if (result == 0){
            redisTemplate.opsForValue().set("promo_item_stock_invalid_" + itemId,"true");
            return true;
        }else {
            increaseSales(itemId,amount);
            return false;
        }

    }


    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) {
        itemDOMapper.increaseSales(itemId,amount);
    }

    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {
        StockLogDO stockLogDO = new StockLogDO();
        stockLogDO.setStatus(1);
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-",""));

        stockLogDOMapper.insert(stockLogDO);

        return stockLogDO.getStockLogId();
    }

    /**
     * <h2>转化itemModel为itemDO</h2>
     * @param itemModel {@link ItemModel}
     * @return {@link ItemDO}
     */
    private ItemDO convertItemDOFromModel(ItemModel itemModel){
        if (itemModel == null) return null;

        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());

        return itemDO;
    }

    /**
     * <h2>转化itemModel为itemDO</h2>
     * @param itemModel {@link ItemModel}
     * @return {@link ItemStockDO}
     */
    private ItemStockDO convertItemStockDOFromModel(ItemModel itemModel){
        if (itemModel == null) return null;

        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setStock(itemModel.getStock());
        itemStockDO.setItemId(itemModel.getId());

        return itemStockDO;
    }

    /**
     * <h2>转化itemDO与itemStockDO为itemStock</h2>
     * @param itemDO {@link ItemDO}
     * @param itemStockDO {@link ItemStockDO}
     * @return {@link ItemModel}
     */
    private ItemModel convertModelFromDataObject(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();

        BeanUtils.copyProperties(itemDO,itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());

        return itemModel;
    }
}
