package com.utility.utility_middleware.model;

import lombok.Data;

@Data
public class authenticationRequest {
    private String email;
    private String password;

}
