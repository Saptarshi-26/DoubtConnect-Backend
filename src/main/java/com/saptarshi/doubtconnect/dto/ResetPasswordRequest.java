package com.saptarshi.doubtconnect.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String token;

    private String newPassword;

}
