package com.kabgig.CoinBot.Telegram.service;

import com.kabgig.CoinBot.CoinMarketCap.service.CoinMarketCapService;
import com.kabgig.CoinBot.CoinMarketCap.service.UserCoinsService;
import com.kabgig.CoinBot.Telegram.entity.ActiveChat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.kabgig.CoinBot.Utils.Logger.lgr;

@Component
public class NotificationService {
    @Autowired
    private CoinMarketCapService coinMarketCapService;
    @Autowired
    private BotService botService;
    @Autowired
    private ActiveChatService activeChatService;

    @Scheduled(cron = "0 0 9 * * *")
    @Bean
    public void dbRefresh() {
        if (!coinMarketCapService.isCurrentdate()) {
            coinMarketCapService.updateDatabase();
            botService.sendText(449744439L, "Executed daily database refresh");
            lgr().info("EXECUTED DAILY DATABASE REFRESH");
        }
    }

    @Scheduled(cron = "0 0 11 * * *")
    @Bean
    public void notifySubscribers() throws InterruptedException {
        List<ActiveChat> uniqueChats = activeChatService.getUniqueUsersChats();
        for (var chat : uniqueChats) {
            String myCoins = botService.getMyCoins(chat.getChatId());
            if (chat.isNotifications()) {
                botService.sendText(chat.getChatId(), "Daily update:" + myCoins);
            }
        }
        lgr().info("DAILY UPDATE SENT " + LocalDateTime.now());
    }


}
