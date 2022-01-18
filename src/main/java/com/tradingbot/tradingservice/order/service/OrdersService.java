package com.tradingbot.tradingservice.order.service;

import com.tradingbot.tradingservice.order.domain.Orders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface OrdersService {
    List<Orders> findByUuid(String uuid);
    List<Orders> findByBotId(String botId);
    Optional<Orders> findById(String id);
    List<Orders> findByUuidAndCoinName(String uuid, String coinName);

    Orders save(Orders orders);
}

