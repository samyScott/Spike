package com.spikeproject.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDO {

    /** 用户id */
    private Integer id;

    /** 用户姓名 */
    private String name;

    /** 用户性别 1:男性 2:女性 */
    private Byte gender;

    /** 用户年龄 */
    private Integer age;

    /** 用户联系方式 */
    private String telphone;

    /** 用户注册方式 byphone,bywechat,byalipay */
    private String registerMode;

    /** 用户第三方id */
    private String thirdPartyId;

}