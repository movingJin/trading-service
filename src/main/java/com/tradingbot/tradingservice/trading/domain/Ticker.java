package com.tradingbot.tradingservice.trading.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Ticker {
    private LocalDateTime timeStamp;
    private String coinName;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double volume;
}
