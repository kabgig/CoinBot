package com.kabgig.CoinBot.Telegram.service;

import com.kabgig.CoinBot.CoinMarketCap.CoinMarketCapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.kabgig.CoinBot.Utils.Logger.lgr;

public class Bot extends TelegramLongPollingBot {
    @Autowired
    private CoinMarketCapService coinMarketCapService;

    public static final String COINS = "/coins";

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

        var response = proceedCommand(msg);
        sendText(userid, response);
    }

    private String proceedCommand(Message msg) {
        String cmd = msg.getText();
        if(cmd.equals(COINS)){
            lgr().info("Proceeded command: " + cmd);
            //var res = coinMarketCapService.getCoinNameAndPrice();
            return "Ok";
        }
        return "Wrong command! Start again";
    }

    public void sendText(Long who, String what){
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
