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
import java.util.Optional;

import static com.kabgig.CoinBot.Utils.Logger.lgr;

@Service
public class BotService extends TelegramLongPollingBot {
    @Autowired
    private CoinMarketCapService coinMarketCapService;
    @Autowired
    private UserCoinsService userCoinsService;

    //COMMANDS
    public static final String START = "/start";
    public static final String ADDCOINS = "/addcoins";
    public static final String MYCOINS = "/mycoins";
    public static final String DELETE = "/delete";
    public static final String MENU =
            "Commands menu:\n" +
            "/mycoins - check all your coins\n" +
            "/addcoins - add coins\n" +
            "/delete - delete coin";

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

        if (cmd.equals(START))
            return MENU;

        if (cmd.equals(ADDCOINS))
            return "In order to subscribe send ONE coin symbol i.e. BTC or ETH";

        if (cmd.equals(MYCOINS))
            return getMyCoins(msg) + "\n ---- \n" + MENU;

        if(cmd.equals(DELETE))
            return "Which coin you want to delete?\n" + getDeleteMenu(msg);

        if(cmd.startsWith("/delete"))
            return processDeleteCommand(cmd,msg);

        if (!cmd.equals(MYCOINS) && !cmd.equals(ADDCOINS))
            return processCoinSymbolAndSubscribe(cmd, msg) + "\n ---- \n" + MENU;


        return "Wrong command! Start again" + "\n ---- \n" + MENU;
    }

    private String processDeleteCommand(String cmd, Message msg) {
        String coinSymbol = cmd.substring("/delete".length());
        CurrentData coin = coinMarketCapService.getOneCoinBySymbol(coinSymbol);
        userCoinsService.deleteByCoinAndUserId(coin.getId(), msg.getChatId());
        return coin.getName() + " was deleted from your coins." + "\n ---- \n" + MENU;
    }

    private String getDeleteMenu(Message msg) {
        String resultMenu = "";
        List<UserCoins> userCoins = userCoinsService.getUserCoins(msg.getChatId());
        for(var userCoin : userCoins){
            CurrentData coin = coinMarketCapService.getOneCoinDataById(userCoin.getCoinId());
            resultMenu = resultMenu + "/delete" + coin.getSymbol() + " - delete " + coin.getName() + "\n";
        }
        return resultMenu;
    }

    private String getMyCoins(Message msg) {
        String result = "";
        List<UserCoins> userCoins = userCoinsService.getUserCoins(msg.getChatId());
        List<CurrentData> customCoinList = coinMarketCapService.getCustomCoinList(userCoins);
        for (var item : customCoinList) {
            result = result + "\n\n" +
                    item.getName() + " " +
                    item.getSymbol() + "\n" +
                    item.getUsd_price() + " $";
        }
        return result;
    }

    private String processCoinSymbolAndSubscribe(String cmd, Message msg) {
        List<String> coinSymbols = coinMarketCapService.getCoinSymbolList();
        if (coinSymbols.contains(cmd)) {
            CurrentData coinData = coinMarketCapService.getOneCoinData(cmd);
            Optional<UserCoins> oneUserCoin = userCoinsService.getOneUserCoin(coinData.getId());
            if (oneUserCoin.isEmpty()) {
                userCoinsService.addCoinSubscribtion(
                        msg.getChatId(),
                        coinData.getId());
                lgr().info(cmd + " COIN ADDED TO SUBSCRIPTION");
            } else {
                lgr().info(cmd + " COIN ALREADY EXISTS IN SUBSCRIPTION");
                return cmd + " coin already exists in your subscription.\nTo add another coin\n👇press command\n/addcoins";
            }
        } else {
            lgr().info(cmd + " COIN SYMBOL IS WRONG, TRY AGAIN");
            return cmd + " coin symbol is wrong, try again, \n👇press command\n/addcoins";
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
