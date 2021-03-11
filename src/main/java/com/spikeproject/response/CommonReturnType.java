package com.spikeproject.response;

import lombok.Data;

/**
 * <h1>返回response对象</h1>
 * @author samy
 * @date 2019/12/28 16:59
 */
@Data
public class CommonReturnType {

    /** 返回结果状态，成功success，失败failed */
    private String status;

    /** 返回数据 */
    private Object data;

    public static CommonReturnType success(Object data){
        return create("success",data);
    }

    public static CommonReturnType create(String status,Object data){
        CommonReturnType returnType = new CommonReturnType();
        returnType.setData(data);
        returnType.setStatus(status);

        return returnType;
    }
}
