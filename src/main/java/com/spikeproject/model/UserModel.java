package com.spikeproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <h1>用户model</h1>
 * @author samy
 * @date 2019/12/28 16:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModel implements Serializable {

    /** 用户id */
    private Integer id;

    /** 用户姓名 */
    @NotBlank(message = "用户名不能为空")
    private String name;

    /** 用户性别 1:男性 2:女性 */
    @NotNull(message = "性别不能为空")
    private Byte gender;

    /** 用户年龄 */
    @NotNull(message = "年龄不能为空")
    @Min(value = 0,message = "年龄必须大于0岁")
    @Max(value = 150,message = "年龄必须小于150岁")
    private Integer age;

    /** 用户联系方式 */
    @NotBlank(message = "手机号不能为空")
    private String telphone;

    /** 用户注册方式 byphone,bywechat,byalipay */
    private String registerMode;

    /** 用户第三方id */
    private String thirdPartyId;

    /** 用户加密密码 */
    @NotBlank(message = "密码不能为空")
    private String encrptPassword;
}
