package com.tradingbot.tradingservice.trading.service;

import java.util.stream.Stream;

public class TradingUtils {
    public enum Token {
        BTC, ETH, XRP, ADA, XLM, EOS, LTC, LINK, TRX, BCH;

        public static Stream<Token> stream() {
            return Stream.of(Token.values());
        }
    }

    public static final String PROFIT = "PROFIT";
    public static final String BIDDING_AVG = "BIDDING_AVG";
    public static final String CLOSED_PRICE = "CLOSED_PRICE";
    public static final String QUANTITY = "QUANTITY";
}
