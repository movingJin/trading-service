package com.tradingbot.tradingservice.user.service;

import com.tradingbot.tradingservice.setting.domain.TradeSetting;
import com.tradingbot.tradingservice.user.domain.PortfolioDto;
import com.tradingbot.tradingservice.user.domain.UserInfo;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
public interface UserInfoService {
    boolean existsByUuid(String uuid);
    boolean existsByConnectKey(String uuid);
    List<UserInfo> findAll();
    Optional<UserInfo> findByUuid(String uuid);
    Boolean isApiValidate(String connectKey, String secretKey);
    Double getUserBalance(String uuid);
    Double getUserAssets(String uuid);
    Double getUserProfit(String uuid);
    PortfolioDto getUserPortfolio(String uuid);

    UserInfo save(UserInfo userInfo);
}
