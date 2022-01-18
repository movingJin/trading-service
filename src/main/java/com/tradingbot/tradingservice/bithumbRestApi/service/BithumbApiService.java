package com.tradingbot.tradingservice.bithumbRestApi.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Component
public interface BithumbApiService {
    public Double getBalance(String connectKey, String secretKey);
    public String marketBidding(String connectKey, String secretKey, String coinName, Double quantity);
    public String marketSell(String connectKey, String secretKey, String coinName, Double quantity);
}
