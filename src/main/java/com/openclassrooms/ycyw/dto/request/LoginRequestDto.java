package com.openclassrooms.ycyw.dto.request;


import lombok.Data;

/**
 * Request DTO used for login request
 * @author Wilhelm Zwertvaegher
 * Date:08/11/2024
 * Time:14:34
 */

@Data
public class LoginRequestDto {

    private String username;

    private String password;
}
