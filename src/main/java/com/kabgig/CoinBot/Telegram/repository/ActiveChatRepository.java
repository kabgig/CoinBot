package com.kabgig.CoinBot.Telegram.repository;

import com.kabgig.CoinBot.Telegram.entity.ActiveChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiveChatRepository extends JpaRepository<ActiveChat, Long> {
    ActiveChat findByChatId(Long chatId);
    @Query("SELECT DISTINCT a.chatId FROM ActiveChat a")
    List<Long> findDistinctChatId();
}
