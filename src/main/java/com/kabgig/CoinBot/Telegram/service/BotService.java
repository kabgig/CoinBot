package com.kabgig.CoinBot.Telegram.service;

import com.kabgig.CoinBot.CoinMarketCap.entity.CurrentData;
import com.kabgig.CoinBot.CoinMarketCap.entity.UserCoins;
import com.kabgig.CoinBot.CoinMarketCap.service.CoinMarketCapService;
import com.kabgig.CoinBot.CoinMarketCap.service.UserCoinsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.kabgig.CoinBot.Telegram.service.ActiveChatService.checkUser;
import static com.kabgig.CoinBot.Utils.Logger.lgr;

@Service
public class BotService extends TelegramLongPollingBot {
    @Autowired
    private CoinMarketCapService coinMarketCapService;
    @Autowired
    private UserCoinsService userCoinsService;

    //COMMANDS
    public static final String COINS = "/coins";
    public static final String ADDCOINS = "/addcoins";
    public static final String MYCOINS = "/mycoins";

    @Override
    public String getBotUsername() {
        return "CoinBot";
    }

    @Override
    public String getBotToken() {
        return "6634826109:AAElkKIzaTJuWfeE0f-Dug8wPTKel8WhnjU";
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var userid = msg.getChatId();
       // checkUser(userid);

        var response = proceedCommand(msg);
        sendText(userid, response);
    }

    private String proceedCommand(Message msg) {
        String cmd = msg.getText();
        lgr().info("Proceeding command: " + cmd);

        if (cmd.equals(COINS))
            return coinMarketCapService.getCoinNameAndPrice();

        if (cmd.equals(ADDCOINS))
            return "In order to subscribe send ONE coin symbol i.e. BTC or ETH";

        if(cmd.equals(MYCOINS))
            return getMyCoins(msg);

        if (!cmd.equals(COINS) && !cmd.equals(ADDCOINS))
            return processCoinSymbolAndSubscribe(cmd, msg);


        return "Wrong command! Start again";
    }

    private String getMyCoins(Message msg) {
        String result = "";
        List<UserCoins> userCoins = userCoinsService.getUserCoins(msg.getChatId());
        List<CurrentData> customCoinList = coinMarketCapService.getCustomCoinList(userCoins);
        for(var item : customCoinList){
            result = result + "\n\n" +
                    item.getName() + " " +
                    item.getSymbol() + "\n" +
                    item.getUsd_price() + "$";
        }
        return result;
    }

    private String processCoinSymbolAndSubscribe(String cmd, Message msg) {
        List<String> coinSymbols = coinMarketCapService.getCoinSymbolList();
        if (coinSymbols.contains(cmd)) {
            CurrentData coinData = coinMarketCapService.getOneCoinData(cmd);
            userCoinsService.addCoinSubscribtion(
                    msg.getChatId(),
                    coinData.getId());
        } else {
            return "Coin Symbol is wrong, try again";
        }

        return cmd + " subscribed!!!!";
    }

    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage
                .builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what)
                .build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
}
