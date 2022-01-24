package com.tradingbot.tradingservice.setting.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tradingbot.tradingservice.setting.domain.TradeSetting;
import com.tradingbot.tradingservice.setting.domain.TradeSettingDto;
import com.tradingbot.tradingservice.setting.domain.TradeSettingMapper;
import com.tradingbot.tradingservice.setting.service.TradeSettingService;
import com.tradingbot.tradingservice.trading.service.TradingService;
import com.tradingbot.tradingservice.user.domain.UserInfo;
import com.tradingbot.tradingservice.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Controller
public class TradeSettingController {
    Logger logger = LoggerFactory.getLogger(getClass());
    private final TradingService tradingService;
    private final TradeSettingService tradeSettingService;
    private final UserInfoService userInfoService;
    private final HashMap<String, Future<Integer> > bidThreads;

    @RequestMapping(value ="/bots", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String createBot(ServerHttpRequest request,
                            @RequestBody TradeSettingDto tradeSettingDto) throws NoSuchAlgorithmException {
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();
        TradeSetting tradeSetting = TradeSettingMapper.INSTANCE.toEntity(tradeSettingDto);

        String tradeSettingId = sha256(uuid + tradeSetting.getBotName());
        if(tradeSettingService.existsById(tradeSettingId)){
            return "duplicate";
        }else{
            tradeSetting.setId(tradeSettingId);
            tradeSetting.setUuid(uuid);
            tradeSettingService.save(tradeSetting);
        }

        return "success";
    }

    @RequestMapping(value ="/bots/{id}", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String updateBot(ServerHttpRequest request,
                                  @PathVariable String id,
                                  @RequestBody TradeSettingDto tradeSettingDto) {
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();
        TradeSetting tradeSetting = TradeSettingMapper.INSTANCE.toEntity(tradeSettingDto);

        if(tradeSetting.getIsActive())
        {
            UserInfo userInfo =  userInfoService.findByUuid(uuid).get();
            Future<Integer> bidThread = tradingService.onTrading(userInfo, tradeSetting);
            bidThreads.put(id, bidThread);
        }
        else
        {
            Future<Integer> bidThread = bidThreads.get(id);
            if(bidThread != null) {
                if (bidThread.isDone()) {
                    try {
                        bidThread.get();
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }
                boolean ret = bidThread.cancel(true);
                logger.info("ret: " + ret);
            }else{
                logger.info(String.format("%s is already stop.", id));
                logger.info("It'll be saved only setting.");
            }
        }

        tradeSetting.setId(id);
        tradeSetting.setUuid(uuid);
        tradeSettingService.save(tradeSetting);

        return "success";
    }

    @RequestMapping(value ="/bots", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Flux<TradeSettingDto> findBots(ServerHttpRequest request) {
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();
        return Flux.fromStream(tradeSettingService.findByUuid(uuid).stream()
                .map(setting -> TradeSettingMapper.INSTANCE.toDto(setting)));
    }

    @RequestMapping(value ="/bots/{id}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Mono<TradeSettingDto> findBot(ServerHttpRequest request, @PathVariable String id) {
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();
        return Mono.just(
                TradeSettingMapper.INSTANCE.toDto(
                        tradeSettingService.findById(id).get()
                )
        );
    }

    @RequestMapping(value ="/bots/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String deleteBot(ServerHttpRequest request, @PathVariable String id) {
        DecodedJWT verify = JWT.decode(Objects.requireNonNull(request
                        .getHeaders()
                        .get(HttpHeaders.AUTHORIZATION))
                .get(0)
                .replace("Bearer ", ""));
        String uuid = verify.getSubject();
        tradeSettingService.deleteById(id);

        return "success";
    }


    public static String sha256(String msg) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(msg.getBytes());

        return bytesToHex(md.digest());
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b: bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
