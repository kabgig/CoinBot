package com.kabgig.CoinBot.Telegram.repository;

import com.kabgig.CoinBot.Telegram.entity.ActiveChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActiveChatRepository extends JpaRepository<ActiveChat, Long> {
    ActiveChat findByChatId(Long chatId);
}
