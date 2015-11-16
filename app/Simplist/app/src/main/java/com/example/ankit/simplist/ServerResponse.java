package com.example.ankit.simplist;

/**
 * Created by santhosh on 11/15/15.
 */
public class ServerResponse {
    private String authToken;

    public ServerResponse(String authToken) {
        this.authToken = authToken;
    }

    public String getToken(){
        return this.authToken;
    }
}
