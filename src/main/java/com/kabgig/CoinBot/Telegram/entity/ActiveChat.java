package com.kabgig.CoinBot.Telegram.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ActiveChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "notifications")
    private boolean notifications;
}
