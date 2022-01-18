package com.tradingbot.tradingservice.user.domain;

import com.tradingbot.tradingservice.trading.service.TradingUtils.Token;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PortfolioDto {
    private Double balance;
    private List<Map<Token, Assets>>tokenAsset;

    public PortfolioDto(){
        this.tokenAsset = new ArrayList<>();
    }

    public PortfolioDto(Double balance){
        this.balance = balance;
        this.tokenAsset = new ArrayList<>();
    }


}

