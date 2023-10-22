package com.kabgig.CoinBot.Telegram.service;

import com.kabgig.CoinBot.CoinMarketCap.service.CoinMarketCapService;
import com.kabgig.CoinBot.CoinMarketCap.service.UserCoinsService;
import com.kabgig.CoinBot.Telegram.entity.ActiveChat;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.kabgig.CoinBot.Utils.Logger.lgr;

@Component
@EnableScheduling
public class NotificationService {
    @Autowired
    private CoinMarketCapService coinMarketCapService;
    @Autowired
    private BotService botService;
    @Autowired
    private ActiveChatService activeChatService;


    @PostConstruct //or @Bean
    @Scheduled(cron = "0 50 23 * * *")
    public void dbRefresh() {
        if (!coinMarketCapService.isCurrentdate()) {
            var res = coinMarketCapService.updateDatabase();
            botService.sendText(botService.getAdminId(), res);
            lgr().info("EXECUTED DAILY DATABASE REFRESH");
        }
    }

    @Scheduled(cron = "0 0 11 * * *")
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
    @Scheduled(cron = "0 0 11 * * *")
    public void regularLogSend() throws InterruptedException {
        lgr().info("SENT " + botService.sendLogs());
    }
    @Scheduled(cron = "0 1 11 * * *")
    public void regularSqlBackupSend() throws InterruptedException {
        lgr().info("SENT " + botService.sendSql());
    }

    @Bean
    private void startupNotification(){
        botService.sendText(botService.getAdminId(), "Bot is started");
    }
}
