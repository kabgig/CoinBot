package com.kabgig.CoinBot.Telegram.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ActiveChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //Уникальный идентификатор в системе нашего бота

    @Column(name = "CHAT_ID")
    private Long chatId; //Уникальный идентификатор в системе Telegram
}
