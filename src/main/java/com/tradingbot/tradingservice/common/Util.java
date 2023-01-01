package com.tradingbot.tradingservice.common;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Objects;

public class Util {
    public static String HTTP_SUCCESS = "success";
    public static String HTTP_FAIL = "fail";
    public static String HTTP_ALREADY_REGISTERED = "already registered";

    public static String getUuidFromToken(ServerHttpRequest request){
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        return verify.getSubject();
    }
}
