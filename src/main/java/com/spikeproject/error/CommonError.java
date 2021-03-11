package com.spikeproject.error;

/**
 * <h1>全局异常错误信息接口</h1>
 * @author samy
 * @date 2019/12/29 19:12
 */
public interface CommonError {

    /**
     * <h2>获取错误码</h2>
     * @return errorCode
     */
    int getErrCode();

    /**
     * <h2>获取错误信息</h2>
     * @return errorMsg
     */
    String getErrMsg();

    /**
     * <h2>设置定制化错误信息</h2>
     * @param errMsg
     * @return {@link CommonError}
     */
    CommonError setErrMsg(String errMsg);

}
