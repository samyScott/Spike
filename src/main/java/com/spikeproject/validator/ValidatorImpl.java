package com.spikeproject.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * <h1>校验器实现类</h1>
 * @author samy
 * @date 2020/1/1 21:29
 */
@Component
public class ValidatorImpl implements InitializingBean {

    /** 校验器 */
    private Validator validator;

    /**
     * <h2>校验并返回校验结果</h2>
     * @param bean 校验对象
     * @return {@link ValidationResult}
     */
    public ValidationResult validate(Object bean){
        ValidationResult result = new ValidationResult();
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);

        if (constraintViolationSet.size() > 0){
            result.setHasErrors(true);
            constraintViolationSet.forEach(violation -> result.getErrorMsgMap()
                    .put(violation.getPropertyPath().toString(),violation.getMessage()));
        }

        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //通过validator工厂初始化一个validator
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
