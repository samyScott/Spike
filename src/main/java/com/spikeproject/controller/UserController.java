package com.spikeproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.spikeproject.error.EmBusinessError;
import com.spikeproject.error.BusinessException;
import com.spikeproject.response.CommonReturnType;
import com.spikeproject.service.IUserService;
import com.spikeproject.model.UserModel;
import com.spikeproject.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *<h1>用户服务ctl</h1>
 * @author samy
 * @date 2019/12/28 15:55
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController {

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private HttpServletRequest request;

    @PostMapping(value = "/login",consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                                  @RequestParam(name = "password")String password)
            throws BusinessException, NoSuchAlgorithmException, UnsupportedEncodingException {
        //入参校验
        if (org.apache.commons.lang3.StringUtils.isEmpty(telphone)
                || org.apache.commons.lang3.StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户名或密码不能为空");
        }

        //用户登录服务,用来校验登录是否合法
        UserModel userModel = userService.validateLogin(telphone, encodeByMd5(password));

        //将登录凭证加入到redis内
        String token = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set(token,convertFromModel(userModel));
        redisTemplate.expire(token,1, TimeUnit.HOURS);
//        HttpSession session = request.getSession();
//        session.setAttribute("IS_LOGIN",true);
//        session.setAttribute("LOGIN_USER",convertFromModel(userModel));


        return CommonReturnType.success(token);
    }


    @PostMapping(value = "/register",consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType register(@RequestParam(name = "telphone")String telphone,
                                     @RequestParam(name = "otpCode")String otpCode,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")Byte gender,
                                     @RequestParam(name = "age")Integer age,
                                     @RequestParam(name = "password")String password)
            throws BusinessException, NoSuchAlgorithmException, UnsupportedEncodingException {
        //验证手机号与对应的optCode是否一致
//        String inRedisOtpCpde = (String) request.getSession().getAttribute(telphone);
        String token = request.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证不正确");
        }
        String inRedisOtpCpde = (String) redisTemplate.opsForValue().get(token);
        if (!StringUtils.equals(inRedisOtpCpde,otpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证不正确");
        }

        //用户注册
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(gender);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.encodeByMd5(password));

        userService.register(userModel);
        return CommonReturnType.success(null);
    }


    //用户获取otp短信接口
    @PostMapping(value = "/getotp",consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType getOtp(@RequestParam(name = "telphone")String telphone){
        //按照一定规则生成OTP验证码
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);

        for (int i = 0;i < 6;i ++){
            sb.append(random.nextInt(10));
        }

        //将OTP验证码同对应的手机号关联，采用redis进行存储
//        request.getSession().setAttribute(telphone,sb.toString());
        String token = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set(token,sb.toString());
        redisTemplate.expire(token,5, TimeUnit.MINUTES);

        //将OTP验证码通过短信通道发送给用户
        System.out.println(sb.toString());

        return CommonReturnType.success(token);
    }


    @GetMapping(value = "/get")
    public CommonReturnType getUser(@RequestParam(name = "id")Integer id) throws BusinessException {
        UserModel userModel = userService.getUserById(id);

        if (userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }

        //将将领域模型用户对象转化为可供UI使用的viewobject
        UserVO userVO = convertFromModel(userModel);

        return CommonReturnType.success(userVO);
    }

    /**
     * <h2>将UserModel转换为UserVO</h2>
     * @param userModel {@link UserModel}
     * @return {@link UserVO}
     */
    private UserVO convertFromModel(UserModel userModel){
        if (userModel == null) return null;

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

    private String encodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();

        //加密字符串
        String newStr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newStr;
    }

}
