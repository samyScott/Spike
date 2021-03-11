package com.spikeproject.controller;

/**
 * @author samy
 * @date 2019/12/29 20:42
 */
public class BaseController {

    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";

    /**
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public CommonReturnType handlerException(HttpServletRequest request, Exception ex){
        Map<String,Object> data = new HashMap<>();

        if (ex instanceof BusinessException){
            BusinessException exception = (BusinessException) ex;

            data.put("errCode",exception.getErrCode());
            data.put("errMsg",exception.getErrMsg());
        }else {
            data.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            data.put("errMsg",EmBusinessError.UNKNOWN_ERROR.getErrMsg());
        }

        CommonReturnType returnType = CommonReturnType.create("fail", data);
        return returnType;
    }
    */
}
