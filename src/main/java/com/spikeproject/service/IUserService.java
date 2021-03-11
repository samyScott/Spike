package com.spikeproject.service;

import com.spikeproject.error.BusinessException;
import com.spikeproject.model.UserModel;

/**
 * <h1>用户服务接口定义</h1>
 * @author samy
 * @date 2019/12/28 15:59
 */
public interface IUserService {

    /**
     * <h2>根据用户ID获取对象</h2>
     * @param id 用户id
     * @Return {@link UserModel}
     */
    UserModel getUserById(Integer id);

    /**
     * <h2>根据用户ID在redis中获取对象</h2>
     * @param id 用户id
     * @return {@link UserModel}
     */
    UserModel getUserByIdInCache(Integer id);

    /**
     * <h2>注册用户</h2>
     * @param userModel {@link UserModel}
     */
    void register(UserModel userModel) throws BusinessException;

    /**
     * <h2>用户登录校验</h2>
     * @param telphone 手机号
     * @param encrptPassword 用户加密后的密码
     * @Return {@link UserModel}
     */
    UserModel validateLogin(String telphone,String encrptPassword) throws BusinessException;
}
