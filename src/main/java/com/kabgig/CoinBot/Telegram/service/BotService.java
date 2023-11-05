package com.kabgig.CoinBot.Telegram.service;

import com.kabgig.CoinBot.CoinMarketCap.entity.CurrentData;
import com.kabgig.CoinBot.CoinMarketCap.entity.UserCoins;
import com.kabgig.CoinBot.CoinMarketCap.service.CoinMarketCapService;
import com.kabgig.CoinBot.CoinMarketCap.service.UserCoinsService;
import com.kabgig.CoinBot.Telegram.entity.ActiveChat;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.kabgig.CoinBot.Utils.Logger.lgr;

@Service
@Data
public class BotService extends TelegramLongPollingBot {
    @Autowired
    private CoinMarketCapService coinMarketCapService;
    @Autowired
    private UserCoinsService userCoinsService;
    @Autowired
    private ActiveChatService activeChatService;
    @Autowired
    private DatabaseDumpService databaseDumpService;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Long adminId = 449744439L;
    private boolean isPing = true;
    private String testBotToken = "6505439712:AAGsoPCKiIliurdCLbFxIgaXBrUgqduPNx4";
    private String testBotName = "dummy_bot";
    private String realBotToken = "6634826109:AAElkKIzaTJuWfeE0f-Dug8wPTKel8WhnjU";
    private String realBotName = "CoinBot";

    //CREDENTIALS
    @Override
    public String getBotUsername() {
        return testBotName;
    }

    @Override
    public String getBotToken() {
        return testBotToken;
    }


    //COMMANDS
    public static final String START = "/start";
    public static final String ADDCOINS = "/addcoins";
    public static final String MYCOINS = "/mycoins";
    public static final String UPDATE = "/update";
    public static final String DELETE = "/delete";
    public static final String NOTsON = "/non";
    public static final String NOTsOFF = "/noff";
    public static final String ADMIN_MESSAGE = "adminMessage: ";
    public static final String REFRESH_COINS = "refreshCoins";
    public static final String FAQ = "FAQ:";
    public static final String ADMIN_SET = "setAdmin";
    public static final String MENU =
            "Commands menu:\n" +
                    "/mycoins - check my coins\n" +
                    "/addcoins - add coins\n" +
                    "/update - update coin amount\n" +
                    "/delete - delete coin\n" +
                    "/non - turnOn notifications\n" +
                    "/noff - turnOff notifications";
    public static final String adminFAQ =
            MENU + "\n" +
                    "adminMessage: - (w/space) send admin message\n" +
                    "refreshCoins - refresh database\n" +
                    "setAdmin - set current user to admin\n" +
                    "sqlBackup: - get SQL backup file\n" +
                    "Log: - get log file\n" +
                    "FAQ: - all admin commands";


    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var userid = msg.getChatId();
        activeChatService.checkUser(userid);
        String response;
        if (coinMarketCapService.isUpdateInProgress()) {
            response = "Database update is in progress, Please try again later";
        } else {
            response = proceedCommand(msg);
        }
        if (!response.equals("")) sendText(userid, response);
    }

    private String proceedCommand(Message msg) {
        String cmd = msg.getText();
        lgr().info("PROCEEDING COMMAND: " + cmd);

        if (cmd.equals(START)) return MENU;

        if (cmd.equals(ADDCOINS)) {
            return "In order to subscribe, send coin symbol " +
                    "and amount you have.\n If you don't have crypto, write only symbol.\n\n" +
                    "For example:\n" +
                    "BTC 0.03";
        }

        if (cmd.equals(MYCOINS)) {
            return getMyCoins(msg.getChatId()) + "\n ---- \n" + MENU;
        }

        if (cmd.equals(UPDATE)) {
            lgr().info("/update cmd is being processed");
            return "To update existing coin, type coin symbol and new amount.\n" +
                    "For example:\n" +
                    "BTC 1.37";
        }

        if (cmd.equals(DELETE)) {
            return "Which coin you want to delete?\n" + getDeleteMenu(msg);
        }

        if (cmd.startsWith("/delete")) {
            return processDeleteCommand(cmd, msg);
        }

        if (cmd.equals(NOTsON)) {
            activeChatService.setNotificationsOn(msg.getChatId());
            return "Notifications are ON";
        }

        if (cmd.equals(NOTsOFF)) {
            activeChatService.setNotificationsOff(msg.getChatId());
            return "Notifications are OFF";
        }

        if (cmd.startsWith(ADMIN_MESSAGE)) {
            processAdminMessage(msg.getText());
            return "Admin message is processed";
        }

        if (cmd.equalsIgnoreCase("log:")) {
            return sendLogs();
        }

        if (cmd.toLowerCase().startsWith("log:-")) {
            int i = Integer.parseInt(cmd.substring(5));
            return sendOldLogs(i);
        }

        if (cmd.equalsIgnoreCase("sqlBackup:")) {
            var res = databaseDumpService.dumpTable(userCoinsService.getAllUserCoins());
            sendText(adminId, res);
            return sendSql();
        }

        if (cmd.equals(ADMIN_SET)) {
            return setAdmin(msg);
        }

        if (cmd.equalsIgnoreCase("ping:")) {
            if (isPing) {
                isPing = false;
            } else {
                isPing = true;
            }
            return "Ping is " + isPing;
        }

        if (cmd.equals(REFRESH_COINS)) {
            coinMarketCapService.updateDatabase();
            return "Database is updated";
        }

        if (cmd.equalsIgnoreCase(FAQ)) return adminFAQ;

        if (!cmd.equals(MYCOINS) && !cmd.equals(ADDCOINS)) {
            return processCoinSymbolAndSubscribe(cmd, msg) + "\n ------- \n" + MENU;
        }
        return "Wrong command! Start again" + "\n ---- \n" + MENU;
    }

    private String setAdmin(Message msg) {
        adminId = msg.getChatId();
        return "Admin: " + adminId + " is set";
    }

    private void processAdminMessage(String cmd) {
        cmd = cmd.substring("adminMessage: ".length());
        List<ActiveChat> uniqueChats = activeChatService.getUniqueUsersChats();
        for (var chat : uniqueChats) sendText(chat.getChatId(), cmd);
        lgr().info("ADMIN MESSAGE IS SENT " + LocalDateTime.now() + " " + cmd);
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
        for (var userCoin : userCoins) {
            CurrentData coin = coinMarketCapService.getOneCoinDataById(userCoin.getCoinId());
            resultMenu = resultMenu + "/delete" + coin.getSymbol() + " - delete " + coin.getName() + "\n";
        }
        return resultMenu;
    }

    public String getMyCoins(Long chatId) {
        String result = "";
        List<UserCoins> userCoins = userCoinsService.getUserCoins(chatId);
        List<CurrentData> customCoinList = coinMarketCapService.getCustomCoinList(userCoins);
        double totalValue = 0.0;
        for (CurrentData item : customCoinList) {
            String h24Arrow = " 🟢 ";
            String d7Arrow = " 🟢 ";
            double h24 = roundDouble(item.getUsd_percentChange24h());
            double d7 = roundDouble(item.getUsd_percentChange7d());
            if (h24 < 0) h24Arrow = " 🔴 ";
            if (d7 < 0) d7Arrow = " 🔴 ";
            double amountOfCrypto = userCoinsService.getCryptoAmount(chatId, item.getId());
            double coinPrice = roundDouble(item.getUsd_price());
            double value = roundDouble(coinPrice * amountOfCrypto);
            result = result + "\n\n" +
                    item.getName() + " " +
                    item.getSymbol() + "\n" +
                    "🏷️ Coin price: " + coinPrice + " $\n" +
                    "🪙 Your amount: " + amountOfCrypto + "\n" +
                    "💰 Your value: " + value + " $\n" +
                    "%24h " + h24Arrow + h24 + "\n" +
                    "%7d " + d7Arrow + d7 + "\n" +
                    "-------";
            totalValue += value;
        }
        return result + "\n\nTotal value: " + totalValue + " $";
    }

    private static double roundDouble(double item) {
        return Math.round(item * 100.0) / 100.0;
    }

    private String processCoinSymbolAndSubscribe(String cmd, Message msg) {
        List<String> coinSymbols = coinMarketCapService.getCoinSymbolList();
        if (cmd.contains(",")) return "Don't use comma ','\n Use dot '.' instead\nTry again";
        String[] cmdArray = cmd.trim().toUpperCase().split(" ");
        cmd = cmdArray[0];
        double amountOfCrypto = 0.0;
        if (cmdArray.length > 1) amountOfCrypto = Double.parseDouble(cmdArray[1]);

        if (coinSymbols.contains(cmd)) {
            CurrentData coinData = coinMarketCapService.getOneCoinData(cmd);
            Optional<UserCoins> oneUserCoin = userCoinsService.getOneUserCoinByChatIdAndCoinId(msg.getChatId(), coinData.getId());
            if (oneUserCoin.isEmpty()) {
                userCoinsService.addCoinSubscribtion(
                        msg.getChatId(),
                        coinData.getId(),
                        amountOfCrypto);
                coinMarketCapService.currentDataCacheRefresh();
                lgr().info(cmd + " COIN ADDED TO SUBSCRIPTION");
            } else {
                UserCoins existingCoin = oneUserCoin.get();
                existingCoin.setAmount(amountOfCrypto);
                userCoinsService.deleteByCoinAndUserId(existingCoin.getCoinId(), msg.getChatId());
                userCoinsService.addCoinSubscribtion(
                        existingCoin.getChatId(),
                        existingCoin.getCoinId(),
                        existingCoin.getAmount());
                lgr().info(cmd + " COIN UPDATED WITH NEW AMOUNT: " + amountOfCrypto);
                return cmd + " coin is updated with new amount: " + amountOfCrypto;
            }
        } else {
            lgr().info(cmd + " COIN SYMBOL IS WRONG, TRY AGAIN");
            return cmd + " coin symbol is wrong,\n" +
                    "check correct symbol spelling at coinmarketcap.com and try again, \n👇press command\n/addcoins";
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
            sendText(adminId, e.toString());
            System.out.println(e);      //Any error will be printed here
        }
    }

    public String sendLogs() {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(formatter);
        sendDocument("logs/log-" + date + ".txt", "Logs");
        return "";
    }
    private String sendOldLogs(int daysBack) {
        LocalDateTime oldDay = LocalDateTime.now().minusDays(daysBack);
        String date = oldDay.format(formatter);
        sendDocument("logs/log-" + date + ".txt", "Logs");
        return "";
    }

    public String sendSql() {
        sendDocument("sqlBackups/userCoins.xlsx", "SQL backup");
        return "";
    }

    private void sendDocument(String pathname, String caption) {
        InputFile inputFile = new InputFile(new File(pathname));
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(adminId.toString());
        sendDocumentRequest.setDocument(inputFile);
        sendDocumentRequest.setCaption(caption);
        try {
            execute(sendDocumentRequest);
        } catch (TelegramApiException e) {
            sendText(adminId, e.toString());
            System.out.println(e);
        }
    }
}
