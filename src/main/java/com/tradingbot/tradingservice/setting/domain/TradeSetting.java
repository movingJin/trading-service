package com.tradingbot.tradingservice.setting.domain;

import lombok.*;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trade_setting")
public class TradeSetting {
    @Id
    @Column(name = "id", unique = true, nullable = false)
    private String id;

    @Column(name = "uuid", nullable = false)
    private String uuid;

    @Column(name = "bot_name", nullable = false)
    private String botName;

    @Column(name = "coin_name", nullable = false)
    private String coinName;

    @Column(name = "bid_reference", nullable = false)
    private String bidReference;

    @Column(name = "bid_condition", nullable = false)
    private Double bidCondition;

    @Column(name = "bid_quantity", nullable = false)
    private Double bidQuantity;

    @Column(name = "is_bid_condition_exceed", nullable = false, columnDefinition = "boolean")
    private Boolean isBidConditionExceed;

    @Column(name = "ask_reference", nullable = false)
    private String askReference;

    @Column(name = "ask_condition", nullable = false)
    private Double askCondition;

    @Column(name = "ask_quantity", nullable = false)
    private Double askQuantity;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean")
    private Boolean isActive;

    @Column(name = "profit")
    private Double profit;

    @Column(name = "description")
    private String description;

}
