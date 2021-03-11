package com.spikeproject.service.impl;

import com.spikeproject.dao.OrderDOMapper;
import com.spikeproject.dao.SequenceDOMapper;
import com.spikeproject.dao.StockLogDOMapper;
import com.spikeproject.dataobject.OrderDO;
import com.spikeproject.dataobject.SequenceDO;
import com.spikeproject.dataobject.StockLogDO;
import com.spikeproject.error.EmBusinessError;
import com.spikeproject.error.BusinessException;
import com.spikeproject.model.ItemModel;
import com.spikeproject.model.OrderModel;
import com.spikeproject.model.UserModel;
import com.spikeproject.service.IItemService;
import com.spikeproject.service.IOrderService;
import com.spikeproject.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author samy
 * @date 2020/1/4 21:54
 */
@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private IItemService itemService;

    @Autowired
    private IUserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId,Integer promoId, Integer amount,String stockLogId) throws BusinessException {
        //校验下单状态,下单商品是否存在，用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }
        if (amount <= 0 || amount > 99){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"数量信息不正确");
        }

        //更新redis库存
        if (!itemService.decreaseStockInCache(itemId, amount)){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setPromoId(promoId);
        orderModel.setAmount(amount);
        if (promoId == null){
            orderModel.setItemPrice(itemModel.getPrice());
        }else {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生成交易流水号
        orderModel.setId(orderService.generateOrderNo());
        OrderDO orderDO = convertOrderDOFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //加上商品的销量
        itemService.increaseSales(itemId,amount);

        //更新订单生成标记位
        StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
        if (stockLogDO == null){
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }
        stockLogDO.setStatus(2);
        stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);

        //返回前端
        return orderModel;
    }

    @Override
    @Transactional
    public String generateOrderNo(){
        //订单号有16位
        StringBuilder sb = new StringBuilder(16);

        //前8位时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        sb.append(nowDate);

        //中间6位为自增序列
        SequenceDO sequence = sequenceDOMapper.getSequenceByName("order_info");
        Integer currentValue = sequence.getCurrentValue();
        sequence.setCurrentValue(currentValue + sequence.getStep());
        sequenceDOMapper.updateByPrimaryKey(sequence);
        String sequenceStr = String.valueOf(sequence.getCurrentValue());
        for (int i = 0;i < 6 - sequenceStr.length();i ++){
            sb.append(0);
        }
        sb.append(sequenceStr);

        //最后2位为分库分表位
        sb.append("00");

        return sb.toString();
    }

    /**
     * <h2>转化orderModel为orderDO</h2>
     * @param orderModel {@link OrderModel}
     * @return {@link OrderDO}
     */
    private OrderDO convertOrderDOFromOrderModel(OrderModel orderModel) {
        if (orderModel == null){
            return null;
        }
        OrderDO orderDO = new OrderDO();

        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());

        return orderDO;
    }
}
