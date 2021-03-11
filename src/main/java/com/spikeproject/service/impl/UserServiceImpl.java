package com.spikeproject.service.impl;

import com.spikeproject.dao.UserDOMapper;
import com.spikeproject.dao.UserPasswordDOMapper;
import com.spikeproject.dataobject.UserDO;
import com.spikeproject.dataobject.UserPasswordDO;
import com.spikeproject.error.EmBusinessError;
import com.spikeproject.error.BusinessException;
import com.spikeproject.service.IUserService;
import com.spikeproject.model.UserModel;
import com.spikeproject.validator.ValidationResult;
import com.spikeproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * @author samy
 * @date 2019/12/28 16:01
 */
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public UserModel getUserById(Integer id) {
        //调用userdaomapper获取到对应用户的dataobject
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null) return null;

        //通过用户id获取加密密码信息
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        return convertFromDataObject(userDO,userPasswordDO);
    }

    @Override
    public UserModel getUserByIdInCache(Integer id) {
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get("user_validate_" + id);

        if (userModel == null){
            userModel = this.getUserById(id);
            redisTemplate.opsForValue().set("user_validate_" + id,userModel);
            redisTemplate.expire("user_validate_" + id,10, TimeUnit.MINUTES);
        }

        return userModel;
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        ValidationResult validate = validator.validate(userModel);
        if (validate.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,validate.getErrMsg());
        }

        UserDO userDO = convertFromModel(userModel);
        try {
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已经存在");
        }

        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel,userDO);
        userPasswordDOMapper.insertSelective(userPasswordDO);
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
       //通过手机获取用户信息
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if (userDO == null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        //校验
        if (!com.alibaba.druid.util.StringUtils.equals(encrptPassword,userPasswordDO.getEncrptPassword())){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }

        return convertFromDataObject(userDO,userPasswordDO);
    }

    /**
     * <h2>转化userModel为userPasswordDO</h2>
     * @param userModel {@link UserModel}
     * @return {@link UserPasswordDO}
     */
    private UserPasswordDO convertPasswordFromModel(UserModel userModel,UserDO userDO){
        if (userModel == null) return null;

        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userDO.getId());

        return userPasswordDO;
    }

    /**
     * <h2>转化userModel为userDO</h2>
     * @param userModel {@link UserModel}
     * @return {@link UserDO}
     */
    private UserDO convertFromModel(UserModel userModel){
        if (userModel == null) return null;

        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);

        return userDO;
    }

    /**
     * <h2>整合UserDO与UserPasswordDO为UserModel</h2>
     * @param userDO {@link UserDO}
     * @param userPasswordDO {@link UserPasswordDO}
     * @return {@link UserModel}
     */
    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if (userDO == null) return null;

        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);

        if (userPasswordDO != null){
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }

        return userModel;
    }
}
