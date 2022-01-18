package com.tradingbot.tradingservice.setting.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeSettingDto {
    private String id;
    private String uuid;
    private String botName;
    private String coinName;
    private String bidReference;
    private Double bidCondition;
    private Double bidQuantity;
    private Boolean isBidConditionExceed;
    private String askReference;
    private Double askCondition;
    private Double askQuantity;
    private Boolean isActive;
    private Double profit;
    private String description;

}
