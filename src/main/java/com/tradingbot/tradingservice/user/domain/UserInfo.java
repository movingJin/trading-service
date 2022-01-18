package com.tradingbot.tradingservice.user.domain;

import lombok.Data;
import javax.persistence.*;

@Entity
@Data
@Table(name = "user_info")
public class UserInfo {
    @Id
    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;

    @Column(name = "connect_key", unique = true)
    private String connectKey;

    @Column(name = "secret_key", unique = true)
    private String secretKey;

    public UserInfo(){}

    public UserInfo(String uuid){
        this.uuid = uuid;
    }
}
