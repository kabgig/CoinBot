package com.kabgig.CoinBot.Telegram;

import com.kabgig.CoinBot.Telegram.service.BotService;
import com.kabgig.CoinBot.Utils.Logger;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@AllArgsConstructor
public class BotStarter {
    @Autowired
    private final BotService botService;

    @Bean
    public void botStart() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(botService);
        Logger.lgr().info("Bot STARTED");
    }
}
