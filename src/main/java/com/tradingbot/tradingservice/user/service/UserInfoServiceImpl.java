package com.tradingbot.tradingservice.user.service;

import com.tradingbot.tradingservice.bithumbRestApi.service.BithumbApiService;
import com.tradingbot.tradingservice.trading.service.TradingUtils;
import com.tradingbot.tradingservice.user.domain.Assets;
import com.tradingbot.tradingservice.user.domain.PortfolioDto;
import com.tradingbot.tradingservice.user.domain.UserInfo;
import com.tradingbot.tradingservice.user.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.tradingbot.tradingservice.trading.service.TradingUtils.*;

@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService, InitializingBean {
    private final UserInfoRepository userInfoRepository;
    private final BithumbApiService bithumbApiService;
    private final RedisTemplate<String, String> redisTemplate;
    private HashOperations<String, String, String> hashOperations;  //Thread safe

    @Override   //PostConstruct 대신 사용
    public void afterPropertiesSet() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public boolean existsByUuid(String uuid) {
        return userInfoRepository.existsByUuid(uuid);
    }

    @Override
    public boolean existsByConnectKey(String connectKey) {
        return userInfoRepository.existsByConnectKey(connectKey);
    }

    @Override
    public List<UserInfo> findAll() {
        return userInfoRepository.findAll();
    }

    @Override
    public Optional<UserInfo> findByUuid(String uuid) {
        return userInfoRepository.findByUuid(uuid);
    }

    @Override
    public Boolean isApiValidate(String connectKey, String secretKey){
        return bithumbApiService
                .getBalance(connectKey, secretKey) != null
                ? true: false;
    }

    @Override
    public Double getUserBalance(String uuid){
        UserInfo userInfo = userInfoRepository.findByUuid(uuid).get();
        return bithumbApiService
                .getBalance(userInfo.getConnectKey(), userInfo.getSecretKey());
    }

    @Override
    public Double getUserAssets(String uuid){
        return getEstimateAssets(uuid).doubleValue();
    }

    @Override
    public Double getUserProfit(String uuid){
        BigDecimal assets = getEstimateAssets(uuid);
        if(assets.compareTo(BigDecimal.ZERO) == 0){
            return 0.0;
        }
        else{
            return assets.divide(
                    TradingUtils.Token.stream()
                            .map(token -> new BigDecimal(hashOperations.get(uuid, String.format("%s_%s", token.name(), BIDDING_AVG)))
                                    .multiply(new BigDecimal(hashOperations.get(uuid, String.format("%s_%s", token.name(), QUANTITY)))))
                            .reduce(BigDecimal.ZERO, BigDecimal::add),6, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100))
                    .subtract(BigDecimal.valueOf(100))
                    .doubleValue();
        }
    }

    @Override
    public PortfolioDto getUserPortfolio(String uuid){
        UserInfo userInfo = userInfoRepository.findByUuid(uuid).get();
        PortfolioDto portfolioDto = new PortfolioDto(bithumbApiService
                .getBalance(userInfo.getConnectKey(), userInfo.getSecretKey()));
        Token.stream()
                .forEach( token -> {
                    portfolioDto.getTokenAsset()
                            .add(Map.of(token, new Assets(
                                    Double.parseDouble(hashOperations.get(token.name(), CLOSED_PRICE)),
                                    Double.parseDouble(hashOperations.get(uuid, String.format("%s_%s", token.name(), QUANTITY)))
                            )));
                });
        return portfolioDto;
    }

    @Override
    public UserInfo save(UserInfo userInfo) {
        Token.stream().forEach(
                token -> {
                    hashOperations.put(
                            userInfo.getUuid(),
                            String.format("%s_%s", token.name(), BIDDING_AVG),
                            BigDecimal.ZERO.toString());
                    hashOperations.put(
                            userInfo.getUuid(),
                            String.format("%s_%s", token.name(), QUANTITY),
                            BigDecimal.ZERO.toString());
                });

        return userInfoRepository.save(userInfo);
    }

    private BigDecimal getEstimateAssets(String uuid){
        return TradingUtils.Token.stream()
                .map(token -> new BigDecimal(hashOperations.get(token.name(), CLOSED_PRICE))
                        .multiply(new BigDecimal(hashOperations.get(uuid, String.format("%s_%s", token.name(), QUANTITY)))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
