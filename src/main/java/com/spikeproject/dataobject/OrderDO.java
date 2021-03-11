package com.spikeproject.dataobject;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDO {

    /** 订单id */
    private String id;

    /** 用户id */
    private Integer userId;

    /** 商品id */
    private Integer itemId;

    /** 秒杀活动id */
    private Integer promoId;

    /** 商品单价 */
    private Double itemPrice;

    /** 购买数量 */
    private Integer amount;

    /** 购买金额 */
    private Double orderPrice;

}