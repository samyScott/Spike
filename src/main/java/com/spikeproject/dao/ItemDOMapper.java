package com.spikeproject.dao;

import com.spikeproject.dataobject.ItemDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemDOMapper {

    /**
     * <h2>根据商品id删除指定商品</h2>
     * @param id 商品id
     * @return 更新的行数
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * <h2>添加商品信息</h2>
     * @param record {@link ItemDO}
     * @return 更新的行数
     */
    int insert(ItemDO record);

    /**
     * <h2>根据字段添加商品信息</h2>
     * @param record {@link ItemDO}
     * @return 更新的行数
     */
    int insertSelective(ItemDO record);

    /**
     * <h2>根据商品id查询商品详细信息</h2>
     * @param id 商品id
     * @return {@link ItemDO}
     */
    ItemDO selectByPrimaryKey(Integer id);

    /**
     * <h2>有选择的更新商品</h2>
     * @param record {@link ItemDO}
     * @return 更新的行数
     */
    int updateByPrimaryKeySelective(ItemDO record);

    /**
     * <h2>更新商品</h2>
     * @param record {@link ItemDO}
     * @return 更新的行数
     */
    int updateByPrimaryKey(ItemDO record);

    /**
     * <h2>查询所有商品</h2>
     * @return {@link ItemDO}
     */
    List<ItemDO> listItem();

    void increaseSales(@Param("itemId")Integer itemId,@Param("amount")Integer amount);
}