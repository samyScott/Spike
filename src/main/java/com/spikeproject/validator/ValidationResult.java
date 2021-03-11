package com.spikeproject.validator;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>校验结果类</h1>
 * @author samy
 * @date 2020/1/1 21:23
 */
@Data
public class ValidationResult {

    /** 校验结果是否有错 */
    private boolean hasErrors = false;

    /** 存放错误信息map */
    private Map<String,String> errorMsgMap = new HashMap<>();

    /**
     * <h2>格式化字符串信息获取错误结果</h2>
     * @return
     */
    public String getErrMsg(){
        return StringUtils.join(errorMsgMap.values().toArray(), ",");
    }
}
