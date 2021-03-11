package com.spikeproject.mq;

import com.alibaba.fastjson.JSON;
import com.spikeproject.dao.StockLogDOMapper;
import com.spikeproject.dataobject.StockLogDO;
import com.spikeproject.error.BusinessException;
import com.spikeproject.service.IOrderService;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author samy
 * @date 2020/1/15 21:06
 */
@Component
public class MqProducer {

    private TransactionMQProducer producer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @PostConstruct
    public void init() throws MQClientException {
        producer = new TransactionMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);

        producer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                Integer userId = (Integer) ((Map)o).get("userId");
                Integer itemId = (Integer) ((Map)o).get("itemId");
                Integer promoId = (Integer) ((Map)o).get("promoId");
                Integer amount = (Integer) ((Map)o).get("amount");
                String stockLogId = (String) ((Map)o).get("stockLogId");

                try {
                    orderService.createOrder(userId,itemId,promoId,amount,stockLogId);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                    stockLogDO.setStatus(3);
                    stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);

                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }

                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                String jsonString = new String(messageExt.getBody());

                Map<String,Object> map = JSON.parseObject(jsonString,Map.class);
                String stockLogId = (String) map.get("stockLogId");

                StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                if (stockLogDO == null){
                    return LocalTransactionState.UNKNOW;
                }
                if (stockLogDO.getStatus() == 2){
                    return LocalTransactionState.COMMIT_MESSAGE;
                }else if (stockLogDO.getStatus() == 1){
                    return LocalTransactionState.UNKNOW;
                }else{
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }
        });

        producer.start();
    }

    /**
     * <h2>事务异步扣减库存</h2>
     * @param userId 用户id
     * @param itemId 商品id
     * @param promoId 秒杀活动id
     * @param amount 扣减数量
     * @return true/false
     */
    public boolean transactionAsyncReduceStock(Integer userId,Integer itemId,Integer promoId,Integer amount,String stockLogId){
        Map<String,Object> msg = new HashMap<>();
        msg.put("itemId",itemId);
        msg.put("amount",amount);
        msg.put("stockLogId",stockLogId);

        Map<String,Object> args = new HashMap<>();
        args.put("userId",userId);
        args.put("itemId",itemId);
        args.put("promoId",promoId);
        args.put("amount",amount);
        args.put("stockLogId",stockLogId);

        Message message = new Message(topicName,"increase",
                JSON.toJSON(msg).toString().getBytes(Charset.forName("UTF-8")));

        TransactionSendResult sendResult;
        try {
           sendResult  = producer.sendMessageInTransaction(message, args);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }

        if (sendResult.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE){
            return false;
        }else if (sendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE){
            return true;
        }else {
            return false;
        }
    }
}
