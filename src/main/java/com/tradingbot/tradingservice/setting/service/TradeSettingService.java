package com.tradingbot.tradingservice.setting.service;

import com.tradingbot.tradingservice.setting.domain.TradeSetting;
import com.tradingbot.tradingservice.user.domain.UserInfo;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
public interface TradeSettingService {
    boolean existsById(String id);
    List<TradeSetting> findAll();
    List<TradeSetting> findByUuid(String uuid);
    List<TradeSetting> findByCoinName(String coinName);
    Optional<TradeSetting> findById(String id);

    TradeSetting save(TradeSetting item);
    void deleteById(String id);
}
