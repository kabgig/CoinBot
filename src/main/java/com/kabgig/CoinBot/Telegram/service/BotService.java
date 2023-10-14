package com.kabgig.CoinBot.Telegram.service;

import com.kabgig.CoinBot.CoinMarketCap.service.CoinMarketCapService;
import com.kabgig.CoinBot.Telegram.entity.ActiveChat;
import com.kabgig.CoinBot.Telegram.repository.ActiveChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.kabgig.CoinBot.Utils.Logger.lgr;

@Service
public class BotService extends TelegramLongPollingBot {
    @Autowired
    private CoinMarketCapService coinMarketCapService;
    @Autowired
    private ActiveChatRepository activeChatRepository;

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
        checkUser(userid);
        sendText(userid, response);
    }

    private void checkUser(Long userid) {
        if(activeChatRepository.findByChatId(userid) == null){
            ActiveChat activeChat = new ActiveChat();
            activeChat.setChatId(userid);
            activeChatRepository.save(activeChat);
        }
    }

    private String proceedCommand(Message msg) {
        String cmd = msg.getText();
        if(cmd.equals(COINS)){
            lgr().info("Proceeded command: " + cmd);
            var res = coinMarketCapService.getCoinNameAndPrice();
            return res;
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
