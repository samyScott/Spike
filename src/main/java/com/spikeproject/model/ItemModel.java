package com.spikeproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <h1>商品model</h1>
 * @author samy
 * @date 2020/1/1 22:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemModel implements Serializable {

    /** 商品id */
    private Integer id;

    /** 商品名称 */
    @NotBlank(message = "商品名称不能为空")
    private String title;

    /** 商品价格 */
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0,message = "商品价格不能低于0元")
    private BigDecimal price;

    /** 商品库存 */
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0,message = "商品库存数量不能为负数")
    private Integer stock;

    /** 商品描述 */
    @NotEmpty(message = "商品描述不能为空")
    private String description;

    /** 商品销量 */
    private Integer sales;

    /** 商品描述图片url */
    @NotEmpty(message = "商品描述图片链接不能为空")
    private String imgUrl;

    /** 该商品秒杀活动 */
    private PromoModel promoModel;

}
