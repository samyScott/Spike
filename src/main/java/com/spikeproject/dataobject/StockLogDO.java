package com.spikeproject.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author samy
 * @date 2020/1/17 21:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockLogDO {

    /** 流水id */
    private String stockLogId;

    /** 商品id */
    private Integer itemId;

    /** 购买数量 */
    private Integer amount;

    /** 下单状态 1.初始状态 2.下单扣库存 3.下单回滚  */
    private Integer status;
}
