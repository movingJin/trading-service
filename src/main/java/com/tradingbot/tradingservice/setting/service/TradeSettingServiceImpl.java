package com.tradingbot.tradingservice.setting.service;

import com.tradingbot.tradingservice.setting.domain.TradeSetting;
import com.tradingbot.tradingservice.setting.repository.TradeSettingRepository;
import com.tradingbot.tradingservice.user.domain.UserInfo;
import com.tradingbot.tradingservice.user.repository.UserInfoRepository;
import com.tradingbot.tradingservice.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tradingbot.tradingservice.trading.service.TradingUtils.*;

@Transactional
@Service
@RequiredArgsConstructor
public class TradeSettingServiceImpl implements TradeSettingService, InitializingBean {
    private final TradeSettingRepository tradeSettingRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private HashOperations<String, String, String> hashOperations;  //Thread safe

    @Override   //PostConstruct 대신 사용
    public void afterPropertiesSet() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public boolean existsById(String id) {
        return tradeSettingRepository.existsById(id);
    }

    @Override
    public List<TradeSetting> findAll() {
        return tradeSettingRepository.findAll();
    }

    @Override
    public List<TradeSetting> findByUuid(String uuid) {
        return tradeSettingRepository.findByUuid(uuid).stream()
                .map(setting -> {
                    String profit = hashOperations.get(setting.getId(), PROFIT);
                    if(profit == null || profit.isEmpty())
                    {
                        setting.setProfit(0.0);
                    }else{
                        setting.setProfit(Double.parseDouble(profit));
                    }
                    return setting;})
                .collect(Collectors.toList());
    }

    @Override
    public List<TradeSetting> findByCoinName(String coinName) {
        return tradeSettingRepository.findByCoinName(coinName);
    }

    @Override
    public Optional<TradeSetting> findById(String id) {

        return tradeSettingRepository.findById(id)
                .map(setting -> {
                    String profit = hashOperations.get(id, PROFIT);
                    if(profit == null || profit.isEmpty())
                    {
                        setting.setProfit(0.0);
                    }else{
                        setting.setProfit(Double.parseDouble(profit));
                    }
                    return setting;
                });
    }

    @Override
    public TradeSetting save(TradeSetting item) {
        hashOperations.put(item.getId(), BIDDING_AVG, BigDecimal.ZERO.toString());
        hashOperations.put(item.getId(), QUANTITY, BigDecimal.ZERO.toString());

        return tradeSettingRepository.save(item);
    }

    @Override
    public void deleteById(String id) {
        tradeSettingRepository.deleteById(id);
    }
}
