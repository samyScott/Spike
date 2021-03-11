package com.spikeproject.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromoDO {

    private Integer id;

    /** 秒杀活动名称 */
    private String promoName;

    /** 秒杀活动开始时间 */
    private Date startDate;

    /** 秒杀活动的商品id */
    private Integer itemId;

    /** 秒杀活动的商品价格 */
    private Double promoItemPrice;

    /** 秒杀活动的结束时间 */
    private Date endDate;
}