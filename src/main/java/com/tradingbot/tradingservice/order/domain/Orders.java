package com.tradingbot.tradingservice.order.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Entity
@Data
@Table(name = "orders")
public class Orders {
    @Id
    @Column(name = "id", unique = true, nullable = false)
    String id;

    @Column(name = "bot_id", nullable = false)
    String botId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Column(nullable = false, name = "time_tag", columnDefinition = "TIMESTAMP")
    private LocalDateTime timeTag;

    @Column(nullable = false, name = "coin_name")
    private String coinName;

    @Column(nullable = false, name = "uuid")
    private String uuid;

    @Column(nullable = false, name = "price")
    private Double price;

    @Column(nullable = false, name = "quantity")
    private Double quantity;

    @Column(name = "is_bid", nullable = false, columnDefinition = "boolean")
    private Boolean isBid;

    @Builder
    public Orders(String id,
                  String botId,
                  String coinName,
                  String uuid,
                  Double price,
                  Double quantity,
                  Boolean isBid){
        this.id = id;
        this.botId = botId;
        this.timeTag = now();
        this.coinName = coinName;
        this.uuid = uuid;
        this.price = price;
        this.quantity = quantity;
        this.isBid = isBid;
    }

    public Orders() {

    }
}
