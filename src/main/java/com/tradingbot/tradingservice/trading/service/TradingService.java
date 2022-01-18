package com.tradingbot.tradingservice.trading.service;

import com.tradingbot.tradingservice.bithumbRestApi.service.BithumbApiService;
import com.tradingbot.tradingservice.order.domain.Orders;
import com.tradingbot.tradingservice.order.service.OrdersService;
import com.tradingbot.tradingservice.setting.domain.TradeSetting;
import com.tradingbot.tradingservice.setting.service.TradeSettingService;
import com.tradingbot.tradingservice.user.domain.UserInfo;
import com.tradingbot.tradingservice.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.tradingbot.tradingservice.trading.service.TradingUtils.*;

@Service
@RequiredArgsConstructor
public class TradingService implements InitializingBean {
    Logger logger = LoggerFactory.getLogger(getClass());
    private final UserInfoService userInfoService;
    private final OrdersService ordersService;
    private final TradeSettingService tradeSettingService;
    private final BithumbApiService bithumbApiService;
    private final RedisTemplate<String, String> redisTemplate;
    private HashOperations<String, String, String> hashOperations;  //Thread safe

    @Override   //PostConstruct 대신 사용
    public void afterPropertiesSet() {
        hashOperations = redisTemplate.opsForHash();

        //트레이딩봇 별, 평균매수금액, 매수수량 Redis에 저장
        tradeSettingService.findAll().stream()
                .forEach(setting -> {
                    Consumer<String> consumer = (botId) ->{
                        final DecimalWrapper quantity = new DecimalWrapper();
                        final DecimalWrapper avg = new DecimalWrapper();
                        ordersService.findByBotId(botId)
                                .stream()
                                .forEach(orders -> {

                                    if(orders.getIsBid()){
                                        avg.value = cumulativeAverage(
                                                avg.value,
                                                quantity.value,
                                                BigDecimal.valueOf(orders.getPrice()),
                                                BigDecimal.valueOf(orders.getQuantity())
                                        );
                                        quantity.value = quantity.value
                                                .add(BigDecimal.valueOf(orders.getQuantity()));
                                    }else{
                                        quantity.value = quantity.value
                                                .subtract(BigDecimal.valueOf(orders.getQuantity()));
                                        if(quantity.value.compareTo(BigDecimal.ZERO) == 0){
                                            avg.value = BigDecimal.ZERO;
                                        }
                                    }
                                });

                        hashOperations.put(botId, QUANTITY, quantity.toString());
                        hashOperations.put(botId, BIDDING_AVG, avg.toString());
                    };
                    consumer.accept(setting.getId());
                });


        //유저 별, 평균매수금액, 매수수량 Redis에 저장
        userInfoService.findAll().stream()
                .forEach(user -> {
                    Token.stream()
                            .forEach( token -> {
                                BiConsumer<String, String> consumer = (uuid, coinName) -> {
                                    final DecimalWrapper quantity = new DecimalWrapper();
                                    final DecimalWrapper avg = new DecimalWrapper();
                                    ordersService.findByUuidAndCoinName(uuid, coinName)
                                            .stream()
                                            .forEach(orders -> {

                                                if(orders.getIsBid()){
                                                    avg.value = cumulativeAverage(
                                                            avg.value,
                                                            quantity.value,
                                                            BigDecimal.valueOf(orders.getPrice()),
                                                            BigDecimal.valueOf(orders.getQuantity())
                                                    );
                                                    quantity.value = quantity.value
                                                            .add(BigDecimal.valueOf(orders.getQuantity()));
                                                }else{
                                                    quantity.value = quantity.value
                                                            .subtract(BigDecimal.valueOf(orders.getQuantity()));
                                                    if(quantity.value.compareTo(BigDecimal.ZERO) == 0){
                                                        avg.value = BigDecimal.ZERO;
                                                    }
                                                }
                                            });

                                    hashOperations.put(uuid, String.format("%s_%s", coinName, QUANTITY), quantity.toString());
                                    hashOperations.put(uuid, String.format("%s_%s", coinName, BIDDING_AVG), avg.toString());

                                };
                                consumer.accept(user.getUuid(), token.name());
                                //hashOperations.put(token.name(), CLOSED_PRICE, "0"); //Redis가 분리되면, 사용
                            });
                });
    }

    private void onBidding(UserInfo userInfo, TradeSetting setting)
    {
        String coinName = setting.getCoinName();
        BigDecimal currentPrice = new BigDecimal(hashOperations.get(setting.getCoinName(), CLOSED_PRICE));
        if((setting.getIsBidConditionExceed()
                && currentPrice.compareTo(
                        new BigDecimal(hashOperations.get(setting.getCoinName(), setting.getBidReference()))
                                .multiply(BigDecimal.valueOf(setting.getBidCondition()).divide(BigDecimal.valueOf(100.0), 6, RoundingMode.HALF_EVEN)))
                > 0) //현재가가 MA보다 큰 경우,
        || (!setting.getIsBidConditionExceed()
                && currentPrice.compareTo(
                        new BigDecimal(hashOperations.get(setting.getCoinName(), setting.getBidReference()))
                                .multiply(BigDecimal.valueOf(setting.getBidCondition()).divide(BigDecimal.valueOf(100.0), 6, RoundingMode.HALF_EVEN)))
                < 0)){ //현재가가 MA보다 작은 경우,
            String order_id = bithumbApiService.marketBidding(userInfo.getConnectKey(), userInfo.getSecretKey(), setting.getCoinName(), setting.getBidQuantity());
            if(order_id != null){
                ordersService.save(
                        new Orders(order_id,
                                setting.getId(),
                                setting.getCoinName(),
                                setting.getUuid(),
                                currentPrice.doubleValue(),
                                setting.getBidQuantity(),
                                true));

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
                String order_id = bithumbApiService.marketSell(userInfo.getConnectKey(), userInfo.getSecretKey(), setting.getCoinName(), setting.getAskQuantity());
                if (order_id != null) {
                    ordersService.save(
                            new Orders(order_id,
                                    setting.getId(),
                                    setting.getCoinName(),
                                    setting.getUuid(),
                                    currentPrice.doubleValue(),
                                    setting.getAskQuantity(),
                                    false));

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
            while (true) {
                if(Thread.currentThread().isInterrupted())
                {
                    logger.info(String.format("%s stop %s's auto trading.", userInfo.getUuid(), tradeSetting.getBotName()));
                    return new AsyncResult<>(1);
                }
                onBidding(userInfo, tradeSetting);
                onSelling(userInfo, tradeSetting);

                Thread.sleep((long) 1000);
            }
        }
        catch (InterruptedException e) {
            logger.error(e.toString());
        }

        return new AsyncResult<>(0);
    }

    public void onRecordProfit(String message){
        JSONObject json_message = new JSONObject(message);
        String coinName = json_message.getString("symbol");
        BigDecimal currentPrice = json_message.getBigDecimal("closePrice");

        tradeSettingService.findByCoinName(coinName).stream()
                .forEach(setting -> {
                    BigDecimal profit = getProfit(setting.getId(), currentPrice);
                    hashOperations.put(setting.getId(), PROFIT, profit.toString());
                });
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

    class DecimalWrapper {
        public BigDecimal value;

        public DecimalWrapper(){
            this.value = BigDecimal.ZERO;
        }
        public DecimalWrapper(BigDecimal value){
            this.value = value;
        }

        @Override
        public String toString(){
            return value.toString();
        }
    }
}
