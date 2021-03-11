package com.spikeproject.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDO {

    /** 商品id */
    private Integer id;

    /** 商品名称 */
    private String title;

    /** 商品价格 */
    private Double price;

    /** 商品描述 */
    private String description;

    /** 商品销量 */
    private Integer sales;

    /** 商品描述图片url */
    private String imgUrl;

}