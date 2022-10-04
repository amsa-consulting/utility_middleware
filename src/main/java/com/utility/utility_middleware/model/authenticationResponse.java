package com.utility.utility_middleware.model;

public class authenticationResponse {
    private final String accessToken;
    //private final String refresh_token;

    public authenticationResponse(String accessToken) {
        this.accessToken = accessToken;
        //this.refresh_token = refresh_token;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
