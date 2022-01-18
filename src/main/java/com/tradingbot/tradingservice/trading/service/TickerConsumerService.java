package com.tradingbot.tradingservice.trading.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TickerConsumerService {
//    private final TradingService tradingService;
//
//    @KafkaListener(
//            topics = {"ADA", "BCH", "BTC", "EOS", "ETH",
//            "LINK", "LTC", "TRX", "XLM", "XRP"}, groupId = "ws-group")
//    public void adaTickerListener(
//            @Payload String message,
//            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition){
//        log.debug("Received Message: \n" + message + "from Partition " + partition);
//        tradingService.onRecordProfit(message);
//    }


}
