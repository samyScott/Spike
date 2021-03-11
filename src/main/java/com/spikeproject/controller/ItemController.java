package com.spikeproject.controller;

import com.spikeproject.error.BusinessException;
import com.spikeproject.model.ItemModel;
import com.spikeproject.response.CommonReturnType;
import com.spikeproject.service.ICacheService;
import com.spikeproject.service.IItemService;
import com.spikeproject.service.IPromoService;
import com.spikeproject.vo.ItemVO;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <h1>商品服务ctl</h1>
 * @author samy
 * @date 2020/1/3 16:43
 */

@RestController
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class ItemController extends BaseController{

    @Autowired
    private IItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ICacheService cacheService;

    @Autowired
    private IPromoService promoService;

    //添加商品信息
    @PostMapping(value = "/create",consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType createItem(@RequestParam(name = "title")String title,
                                       @RequestParam(name = "desc")String desc,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock")Integer stock,
                                       @RequestParam(name = "imgUrl")String imgUrl) throws BusinessException {
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(desc);
        itemModel.setStock(stock);
        itemModel.setPrice(price);
        itemModel.setImgUrl(imgUrl);

        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        ItemVO itemVO = convertVOFromModel(itemModelForReturn);

        return CommonReturnType.success(itemVO);
    }

    @GetMapping(value = "/publishpromo")
    public CommonReturnType publishpromo(@RequestParam(name = "promoId")Integer promoId) {
        promoService.publishPromo(promoId);
        return CommonReturnType.success(null);
    }

    //商品详细信息获取
    @GetMapping(value = "/get")
    public CommonReturnType getItem(@RequestParam(name = "id")Integer id) {
        ItemModel itemModel = null;

        //从本地缓存中获取
        itemModel = (ItemModel) cacheService.getFromCommonCache("item_" + id);
        if (itemModel == null){
            //从redis内获取
            itemModel = (ItemModel) redisTemplate.opsForValue().get("item_" + id);

            if (itemModel == null){
                itemModel = itemService.getItemById(id);
                redisTemplate.opsForValue().set("item_" + id,itemModel);
                redisTemplate.expire("item_" + id,10, TimeUnit.MINUTES);
            }
            cacheService.setCommonCache("item_" + id,itemModel);
        }

        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.success(itemVO);
    }

    //商品列表页面浏览
    @GetMapping("/list")
    public CommonReturnType listItem(){
        List<ItemModel> itemModelList = itemService.listItem();
        List<ItemVO> itemVOList = itemModelList.stream()
                .map(this::convertVOFromModel)
                .collect(Collectors.toList());

        return CommonReturnType.success(itemVOList);
    }

    /**
     * <h2>转化itemModel为itemVO</h2>
     * @param itemModel {@link ItemModel}
     * @return {@link ItemVO}
     */
    private ItemVO convertVOFromModel(ItemModel itemModel){
        if (itemModel == null){
            return null;
        }

        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);
        if (itemModel.getPromoModel() != null){
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
        }else {
            itemVO.setPromoStatus(0);
        }

        return itemVO;
    }


}
