package com.spikeproject.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <h1>用户视图VO</h1>
 * @author samy
 * @date 2019/12/28 16:48
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable{

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

}
