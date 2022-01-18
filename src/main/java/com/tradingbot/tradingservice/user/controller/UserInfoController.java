package com.tradingbot.tradingservice.user.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tradingbot.tradingservice.user.domain.PortfolioDto;
import com.tradingbot.tradingservice.user.domain.UserInfo;
import com.tradingbot.tradingservice.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.tradingbot.tradingservice.common.Util.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserInfoController {
    private final Environment env;
    private final UserInfoService userInfoService;

    @GetMapping("/health_check")
    public String eurekaStatus() {
        return String.format("%s", env.getProperty("spring.application.name"));
    }

    @GetMapping(value = "/user-api")
    public Mono<ResponseEntity<UserInfo>> findUserApi(ServerHttpRequest request) {
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();

        return Mono.just(userInfoService.findByUuid(uuid).orElse(new UserInfo(uuid)))
                .map(user -> ResponseEntity.ok().body(user));
    }

    @PostMapping(value = "/user-api")
    public String updateUserApi(ServerHttpRequest request,
                                @RequestBody UserInfo userInfo) {
        String retMessage;
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();

        if(userInfoService.existsByConnectKey(userInfo.getConnectKey())){
            retMessage = HTTP_ALREADY_REGISTERED;
        }else{
            userInfo.setUuid(uuid);
            userInfoService.save(userInfo);
            retMessage = HTTP_SUCCESS;
        }

        return retMessage;
    }

    @PostMapping(value = "/api-validate")
    public Mono<ResponseEntity<Boolean>> isApiValidate(ServerHttpRequest request
            ,@RequestBody UserInfo userInfo) {
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();

        return Mono.just(userInfoService.isApiValidate(userInfo.getConnectKey(), userInfo.getSecretKey()))
                .map(valid -> ResponseEntity.ok().body(valid));
    }

    @GetMapping(value = "/user-balance")
    public Mono<ResponseEntity<Double>> getBalance(ServerHttpRequest request) {
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();

        return Mono.just(userInfoService.getUserBalance(uuid)).map(user -> ResponseEntity.ok().body(user));
    }

    @GetMapping(value = "/user-assets")
    public Mono<ResponseEntity<Double>> getAssets(ServerHttpRequest request) {
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();

        return Mono.just(userInfoService.getUserAssets(uuid)).map(user -> ResponseEntity.ok().body(user));
    }

    @GetMapping(value = "/user-profit")
    public Mono<ResponseEntity<Double>> getProfit(ServerHttpRequest request) {
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();

        return Mono.just(userInfoService.getUserProfit(uuid)).map(user -> ResponseEntity.ok().body(user));
    }

    @GetMapping(value = "/user-portfolio")
    public Mono<ResponseEntity<PortfolioDto>> getPortfolio(ServerHttpRequest request) {
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();

        return Mono.just(userInfoService.getUserPortfolio(uuid))
                .map(portfolio -> ResponseEntity.ok().body(portfolio));
    }
}
