package com.tradingbot.tradingservice.order.controller;

import com.tradingbot.tradingservice.common.Util;
import com.tradingbot.tradingservice.order.domain.Orders;
import com.tradingbot.tradingservice.order.service.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OrdersController {
    private final OrdersService ordersService;

    @RequestMapping(value ="/orders", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Flux<Orders> findOrders(ServerHttpRequest request){
        String uuid = Util.getUuidFromToken(request);

        return Flux.fromStream(ordersService
                .findByUuidAndTimeTagAfterOrderByTimeTagDesc(uuid, LocalDateTime.now().minusDays(90))
                .stream());
    }


    @RequestMapping(value ="/orders/{id}", method = RequestMethod.GET)
    public Mono<ResponseEntity<Orders>> findOrder(ServerHttpRequest request,
                                  @PathVariable String id) {
        String uuid = Util.getUuidFromToken(request);

        return Mono.just(ordersService.findById(id).get()).map(order -> ResponseEntity.ok().body(order));
    }
}
