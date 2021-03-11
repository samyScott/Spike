package com.spikeproject.error;


/**
 * <h1>包装器业务异常类实现</h1>
 * @author samy
 * @date 2019/12/29 19:30
 */
public class BusinessException extends Exception implements CommonError {

    /** 通用错误定义 */
    private CommonError commonError;

    public BusinessException(CommonError commonError){
        this.commonError = commonError;
    }

    public BusinessException(CommonError commonError, String errMsg){
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);
    }

    @Override
    public int getErrCode() {
        return commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return commonError.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }
}
