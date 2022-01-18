package com.tradingbot.tradingservice.order.service;

import com.tradingbot.tradingservice.order.domain.Orders;
import com.tradingbot.tradingservice.order.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {
    private final OrdersRepository ordersRepository;

    @Override
    public List<Orders> findByUuid(String uuid) {
        return ordersRepository.findByUuid(uuid);
    }

    @Override
    public List<Orders> findByBotId(String botId) {
        return ordersRepository.findByBotId(botId);
    }

    @Override
    public Optional<Orders> findById(String id) {
        return ordersRepository.findById(id);
    }

    @Override
    public List<Orders> findByUuidAndCoinName(String uuid, String coinName) {
        return ordersRepository.findByUuidAndCoinName(uuid, coinName);
    }

    @Override
    public Orders save(Orders orders) {
        return ordersRepository.save(orders);
    }
}
