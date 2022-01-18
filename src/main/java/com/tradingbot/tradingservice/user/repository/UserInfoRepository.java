package com.tradingbot.tradingservice.user.repository;

import com.tradingbot.tradingservice.setting.domain.TradeSetting;
import com.tradingbot.tradingservice.user.domain.UserInfo;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    boolean existsByUuid(String uuid);
    boolean existsByConnectKey(String uuid);
    List<UserInfo> findAll();
    Optional<UserInfo> findByUuid(String uuid);

    UserInfo save(TradeSetting item);
}

