package com.tradingbot.tradingservice;

import com.tradingbot.tradingservice.bithumbRestApi.service.BithumbApiService;
import com.tradingbot.tradingservice.order.domain.Orders;
import com.tradingbot.tradingservice.setting.domain.TradeSetting;
import com.tradingbot.tradingservice.setting.service.TradeSettingService;
import com.tradingbot.tradingservice.user.domain.UserInfo;
import com.tradingbot.tradingservice.user.service.UserInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Future;

import static com.tradingbot.tradingservice.trading.service.TradingUtils.*;
import static com.tradingbot.tradingservice.trading.service.TradingUtils.QUANTITY;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class TradingLogicalTest implements InitializingBean {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private HashOperations<String, String, String> hashOperations;  //Thread safe
    @Autowired
    private TradeSettingService tradeSettingService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private BithumbApiService bithumbApiService;

    //given
    private static int order_count = 0;
    private static Double balance = 50000000.0;

    @Override   //PostConstruct 대신 사용
    public void afterPropertiesSet() {
        hashOperations = redisTemplate.opsForHash();
    }
    private void onBidding(UserInfo userInfo, TradeSetting setting)
    {
        String coinName = setting.getCoinName();
        BigDecimal currentPrice = new BigDecimal(hashOperations.get(setting.getCoinName(), CLOSED_PRICE));
        BigDecimal biddingCondition =
                new BigDecimal(hashOperations.get(setting.getCoinName(), setting.getBidReference()))
                        .multiply(BigDecimal.valueOf(setting.getBidCondition()).divide(BigDecimal.valueOf(100.0), 6, RoundingMode.HALF_EVEN));
        if((setting.getIsBidConditionExceed() && currentPrice.compareTo(biddingCondition) > 0) //현재가가 MA보다 큰 경우,
                || (!setting.getIsBidConditionExceed() && currentPrice.compareTo(biddingCondition) < 0)){ //현재가가 MA보다 작은 경우,
            String order_id = marketBidding(currentPrice.doubleValue(), setting.getBidQuantity());
            logger.info(coinName + " buy at " + currentPrice + ", order_id: " + order_id);
            if(order_id != null){

                BigDecimal quantityFromBot = new BigDecimal(hashOperations.get(setting.getId(), QUANTITY));
                BigDecimal prevAvgFromBot = new BigDecimal(hashOperations.get(setting.getId(), BIDDING_AVG));
                hashOperations.put(setting.getId(),
                        BIDDING_AVG,
                        cumulativeAverage(
                                prevAvgFromBot,
                                quantityFromBot,
                                currentPrice,
                                BigDecimal.valueOf(setting.getBidQuantity())
                        ).toString());

                hashOperations.put(
                        setting.getId(),
                        QUANTITY,
                        quantityFromBot.add(BigDecimal.valueOf(setting.getBidQuantity()))
                                .toString()
                );


                BigDecimal quantityFromUser = new BigDecimal(hashOperations.get(setting.getUuid(), String.format("%s_%s", coinName, QUANTITY)));
                BigDecimal prevAvgFromUser = new BigDecimal(hashOperations.get(userInfo.getUuid(), String.format("%s_%s", coinName, BIDDING_AVG)));
                hashOperations.put(setting.getUuid(),
                        String.format("%s_%s", coinName, BIDDING_AVG),
                        cumulativeAverage(
                                prevAvgFromUser,
                                quantityFromUser,
                                currentPrice,
                                BigDecimal.valueOf(setting.getBidQuantity())
                        ).toString());
                hashOperations.put(
                        setting.getUuid(),
                        String.format("%s_%s", setting.getCoinName(), QUANTITY),
                        quantityFromUser.add(BigDecimal.valueOf(setting.getBidQuantity()))
                                .toString()
                );
            }
        }
    }

    private void onSelling(UserInfo userInfo, TradeSetting setting)
    {
        String coinName = setting.getCoinName();
        BigDecimal currentPrice = new BigDecimal(hashOperations.get(setting.getCoinName(), CLOSED_PRICE));
        BigDecimal biddingQuantity = new BigDecimal(hashOperations.get(setting.getId(), QUANTITY));
        if(biddingQuantity.compareTo(BigDecimal.ZERO) > 0){ //아직 매수하지 못한 봇의 접근을 막기 위함
            BigDecimal profit = getProfit(setting.getId(), currentPrice);

            if (profit.compareTo(BigDecimal.valueOf(setting.getAskCondition())) > 0) { //현 수익율이 설정한 수익률보다 높은 경우,
                String order_id = marketSell(currentPrice.doubleValue(), setting.getAskQuantity());
                logger.info(coinName + " sell at " + currentPrice + ", order_id: " + order_id);
                if (order_id != null) {

                    BigDecimal quantityFromBot = new BigDecimal(hashOperations.get(setting.getId(), QUANTITY));
                    if(quantityFromBot.compareTo(BigDecimal.ZERO) == 0){ //가진 자산이 없으면, 평균매수가 0 처리
                        hashOperations.put(setting.getId(), BIDDING_AVG, BigDecimal.ZERO.toString());
                    }
                    hashOperations.put(
                            setting.getId(),
                            QUANTITY,
                            quantityFromBot.subtract(BigDecimal.valueOf(setting.getAskQuantity()))
                                    .toString()
                    );


                    BigDecimal quantityFromUser = new BigDecimal(hashOperations.get(setting.getUuid(), String.format("%s_%s", setting.getCoinName(), QUANTITY)));
                    BigDecimal remain = quantityFromUser.subtract(BigDecimal.valueOf(setting.getAskQuantity()));
                    hashOperations.put(
                            setting.getUuid(),
                            String.format("%s_%s", coinName, QUANTITY),
                            remain.toString()
                    );
                }

            }
        }

    }

    @Async
    public Future<Integer> onTrading(UserInfo userInfo, TradeSetting tradeSetting) {
        try {
            //while (true) {
                if(Thread.currentThread().isInterrupted())
                {
                    logger.info(String.format("%s stop %s's auto trading.", userInfo.getUuid(), tradeSetting.getBotName()));
                    return new AsyncResult<>(1);
                }
                Double currentPrice = bithumbApiService.getCurrentPriceByCoin(tradeSetting.getCoinName());
                hashOperations.put(tradeSetting.getCoinName(), "CLOSED_PRICE", currentPrice.toString());
                onBidding(userInfo, tradeSetting);
                onSelling(userInfo, tradeSetting);

                Thread.sleep((long) 1000);
            //}
        }
        catch (InterruptedException e) {
            logger.error(e.toString());
        }

        return new AsyncResult<>(0);
    }

    @Test
    @Rollback(false)
    @DisplayName("거래로직 테스트")
    void onTradingTest() {
        String uuid = "720d6f84-5656-4aa9-9743-5644202bad76";
        String tradingBotId = "385389c37f2abf245438bc15dfcb3b001316571239d108d019b809988e999fbc";
        UserInfo userInfo =  userInfoService.findByUuid(uuid).get();
        TradeSetting tradeSetting= tradeSettingService.findById(tradingBotId).get();
        onTrading(userInfo, tradeSetting);
    }


    private String marketBidding(Double price, Double quantity) {
        String order_id = "" + order_count++;
        balance -= (price * quantity);
        return order_id;
    }

    private String marketSell(Double price, Double quantity) {
        String order_id = "" + order_count++;
        balance += (price * quantity);
        return order_id;
    }

    private BigDecimal cumulativeAverage (BigDecimal prevAvg, BigDecimal oldLength, BigDecimal newNumber, BigDecimal newLength) {
        BigDecimal listLength = oldLength.add(newLength);
        BigDecimal oldWeight = oldLength.divide(listLength, 6, RoundingMode.HALF_EVEN);
        BigDecimal newWeight = newLength.divide(listLength, 6, RoundingMode.HALF_EVEN);
        return prevAvg.multiply(oldWeight).add(newNumber.multiply(newWeight));
    }

    private BigDecimal getProfit(String botId, BigDecimal currentPrice){
        BigDecimal profit = BigDecimal.ZERO;
        BigDecimal biddingAvg = new BigDecimal(hashOperations.get(botId, BIDDING_AVG));
        if(biddingAvg.compareTo(BigDecimal.ZERO) > 0){
            profit = currentPrice.divide(biddingAvg, 6, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100)).subtract(BigDecimal.valueOf(100));
        }

        return profit;
    }
}
