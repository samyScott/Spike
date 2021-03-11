package com.spikeproject.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPasswordDO {

    private Integer id;

    /** 用户加密密码 */
    private String encrptPassword;

    /** 用户id */
    private Integer userId;
}