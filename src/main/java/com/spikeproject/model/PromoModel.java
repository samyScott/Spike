package com.spikeproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * @author samy
 * @date 2020/1/5 20:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromoModel implements Serializable {

    private Integer id;

    /** 秒杀活动状态 1表示未开始,2表示进行中，3表示已结束 */
    private Integer status;

    /** 秒杀活动名称 */
    private String promoName;

    /** 秒杀活动开始时间 */
    private DateTime startDate;

    /** 秒杀活动的结束时间 */
    private DateTime endDate;

    /** 秒杀活动的商品id */
    private Integer itemId;

    /** 秒杀活动的商品价格 */
    private BigDecimal promoItemPrice;

}
