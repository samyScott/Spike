package com.spikeproject.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <h1>商品视图VO</h1>
 * @author samy
 * @date 2020/1/3 16:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemVO implements Serializable {

    /** 商品id */
    private Integer id;

    /** 商品名称 */
    private String title;

    /** 商品价格 */
    private BigDecimal price;

    /** 商品库存 */
    private Integer stock;

    /** 商品描述 */
    private String description;

    /** 商品销量 */
    private Integer sales;

    /** 商品描述图片url */
    private String imgUrl;

    /** 商品是否在秒杀活动中 0没有秒杀活动，1秒杀活动未开始，2秒杀活动进行中 */
    private Integer promoStatus;

    /** 秒杀活动价格 */
    private BigDecimal promoPrice;

    /** 秒杀活动id */
    private Integer promoId;

    /** 秒杀活动开始时间 */
    private String startDate;

}
