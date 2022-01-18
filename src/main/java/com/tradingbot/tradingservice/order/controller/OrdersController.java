package com.tradingbot.tradingservice.order.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tradingbot.tradingservice.order.domain.Orders;
import com.tradingbot.tradingservice.order.service.OrdersService;
import com.tradingbot.tradingservice.setting.domain.TradeSetting;
import com.tradingbot.tradingservice.setting.service.TradeSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OrdersController {
    private final OrdersService ordersService;

    @RequestMapping(value ="/orders", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Flux<Orders> findOrders(ServerHttpRequest request){
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();

        return Flux.fromStream(ordersService
                .findByUuid(uuid)
                .stream());
    }


    @RequestMapping(value ="/orders/{id}", method = RequestMethod.GET)
    public Mono<ResponseEntity<Orders>> findOrder(ServerHttpRequest request,
                                  @PathVariable String id) {
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();

        return Mono.just(ordersService.findById(id).get()).map(order -> ResponseEntity.ok().body(order));
    }
}
