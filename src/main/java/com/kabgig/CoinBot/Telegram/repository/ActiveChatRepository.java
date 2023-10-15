package com.kabgig.CoinBot.Telegram.repository;

import com.kabgig.CoinBot.Telegram.entity.ActiveChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiveChatRepository extends JpaRepository<ActiveChat, Long> {
    ActiveChat findByChatId(Long chatId);
    @Query("SELECT DISTINCT a.chatId FROM ActiveChat a")
    List<Long> findDistinctChatId();
    @Modifying
    @Query("UPDATE ActiveChat a SET a.notifications = true WHERE a.chatId = :chatId")
    void setNotificationsToTrueByChatId(Long chatId);
    @Modifying
    @Query("UPDATE ActiveChat a SET a.notifications = false WHERE a.chatId = :chatId")
    void setNotificationsToFalseByChatId(Long chatId);

}
