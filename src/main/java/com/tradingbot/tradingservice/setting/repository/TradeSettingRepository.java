package com.tradingbot.tradingservice.setting.repository;

import com.tradingbot.tradingservice.setting.domain.TradeSetting;
import com.tradingbot.tradingservice.user.domain.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeSettingRepository extends JpaRepository<TradeSetting, Long> {
    //@Query("SELECT exists  FROM TradeSetting t WHERE u.status = 1")
    boolean existsById(String id);
    List<TradeSetting> findByUuid(String uuid);
    List<TradeSetting> findByCoinName(String coinName);
    Optional<TradeSetting> findById(String id);
    void deleteById(String id);
}

