package com.kabgig.CoinBot.CoinMarketCap.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class UserCoins {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "chat_id")
    private Long chatId;
    @Column(name = "coin_id")
    private Long coinId;
}
