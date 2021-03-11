package com.spikeproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * <h1>用户下单的交易模型</h1>
 * @author samy
 * @date 2020/1/4 21:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderModel {

    /** 订单id */
    private String id;

    /** 用户id */
    private Integer userId;

    /** 商品id */
    private Integer itemId;

    /** 商品单价 */
    private BigDecimal itemPrice;

    /** 秒杀活动id */
    private Integer promoId;

    /** 购买数量 */
    private Integer amount;

    /** 购买金额 */
    private BigDecimal orderPrice;

}
