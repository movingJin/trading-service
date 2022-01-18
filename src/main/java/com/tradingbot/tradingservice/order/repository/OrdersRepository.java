package com.tradingbot.tradingservice.order.repository;

import com.tradingbot.tradingservice.order.domain.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByUuid(String uuid);
    List<Orders> findByBotId(String botId);
    Optional<Orders> findById(String id);
    List<Orders> findByUuidAndCoinName(String uuid, String coinName);
}

