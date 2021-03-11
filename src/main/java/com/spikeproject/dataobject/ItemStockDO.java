package com.spikeproject.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemStockDO {

    private Integer id;

    /** 商品库存 */
    private Integer stock;

    /** 商品id */
    private Integer itemId;


}