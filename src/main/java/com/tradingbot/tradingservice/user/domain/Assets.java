package com.tradingbot.tradingservice.user.domain;

import lombok.Data;

@Data
public
class Assets{
    public Assets(){
    }
    public Assets(Double estimate, Double quantity){
        this.estimate = estimate;
        this.quantity = quantity;
    }
    Double estimate;
    Double quantity;
}
