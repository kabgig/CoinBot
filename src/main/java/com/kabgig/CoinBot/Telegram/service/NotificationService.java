package com.kabgig.CoinBot.Telegram.service;

import com.kabgig.CoinBot.CoinMarketCap.entity.CurrentData;
import com.kabgig.CoinBot.CoinMarketCap.entity.UserCoins;
import com.kabgig.CoinBot.CoinMarketCap.service.CoinMarketCapService;
import com.kabgig.CoinBot.CoinMarketCap.service.UserCoinsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
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
    @Autowired
    private UserCoinsService userCoinsService;

    @Scheduled(cron = "0 0 9 * * *")
    @Bean
    public void dbRefresh() {
        if (coinMarketCapService.isCurrentdate()) {
            coinMarketCapService.updateDatabase();
        }
    }

    @Scheduled(cron = "*/15 * * * * *")
    @Bean
    public void notifySubscribers() throws InterruptedException {
        List<Long> uniqueIds = activeChatService.getUniqueUsersChatIds();
        for (Long id : uniqueIds) {
            String myCoins = botService.getMyCoins(id);
            botService.sendText(id, "Daily update:" + myCoins);
        }
        lgr().info("DAILY UPDATE SENT " + LocalDateTime.now());
    }
}
