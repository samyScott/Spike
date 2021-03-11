package com.spikeproject.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.spikeproject.error.EmBusinessError;
import com.spikeproject.error.BusinessException;
import com.spikeproject.mq.MqProducer;
import com.spikeproject.response.CommonReturnType;
import com.spikeproject.service.IItemService;
import com.spikeproject.service.IPromoService;
import com.spikeproject.util.CodeUtil;
import com.spikeproject.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <h1>订单服务ctl</h1>
 * @author samy
 * @date 2020/1/4 23:25
 */
@RestController
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController{

    @Autowired
    private MqProducer producer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IItemService itemService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IPromoService promoService;

    //流量控制
    private ExecutorService executorService;

    //并发控制
    private RateLimiter orderCreateRateLimiter;
    
    @PostConstruct
    public void init(){
        executorService = Executors.newFixedThreadPool(20);
        orderCreateRateLimiter = RateLimiter.create(300);
    }


    @RequestMapping(value = "/generateverifycode")
    public void generateverifycode(HttpServletResponse response) throws BusinessException, IOException {
        orderCreateRateLimiter.acquire();
        //判断用户是否登录
        UserVO userVO = isLogin();
        if (userVO == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        Map<String,Object> map = CodeUtil.generateCodeAndPic();
        redisTemplate.opsForValue().set("verify_code_" + userVO.getId(),map.get("code"));
        redisTemplate.expire("verify_code_" + userVO.getId(),10,TimeUnit.MINUTES);
        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", response.getOutputStream());
    }


    @PostMapping(value = "/generatetoken",consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType generatetoken(@RequestParam("itemId")Integer itemId,
                                          @RequestParam("promoId")Integer promoId,
                                          @RequestParam("verifyCode")String verifyCode) throws BusinessException {
        orderCreateRateLimiter.acquire();
        //判断用户是否登录
        UserVO userVO = isLogin();
        if (userVO == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        //校验验证码
        String inRedisVerifyCode = (String) redisTemplate.opsForValue().get("verify_code_" + userVO.getId());
        if (StringUtils.isEmpty(inRedisVerifyCode) || !verifyCode.equalsIgnoreCase(inRedisVerifyCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"验证码不正确");
        }

        //生成秒杀令牌
        String spikeToken = promoService.generateSpikeToken(promoId, itemId, userVO.getId());
        if (spikeToken == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"生成令牌失败");
        }

        return CommonReturnType.success(spikeToken);
    }

    @PostMapping(value = "/createorder",consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType createOrder(@RequestParam("itemId")Integer itemId,
                                        @RequestParam("amount")Integer amount,
                                        @RequestParam(value = "promoId",required = false)Integer promoId,
                                        @RequestParam(value = "promoToken",required = false)String promoToken) throws BusinessException {
        orderCreateRateLimiter.acquire();
        //判断用户是否登录
        UserVO userVO = isLogin();
        if (userVO == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        if (promoToken != null){
            //校验令牌
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_" + promoId + "_itemId_" + itemId + "_userId_" + userVO.getId());
            if (inRedisPromoToken == null || !StringUtils.equals(inRedisPromoToken,promoToken)){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌校验失败");
            }
        }

        //判断库存是否已售罄
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        Future<Object> future = executorService.submit(() -> {
            //初始化流水init
            String stockLogId = itemService.initStockLog(itemId, amount);

            //异步更新数据库库存
            if (!producer.transactionAsyncReduceStock(userVO.getId(), itemId, promoId, amount, stockLogId)) {
                throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
            }
            return null;
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }

        return CommonReturnType.success(null);

    }

    /**
     * <h2>判断用户是否登录</h2>
     * @return {@link UserVO}
     */
    private UserVO isLogin(){
        String token = request.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)){
            return null;
        }
        UserVO userVO = (UserVO) redisTemplate.opsForValue().get(token);
        return userVO;
    }

}
