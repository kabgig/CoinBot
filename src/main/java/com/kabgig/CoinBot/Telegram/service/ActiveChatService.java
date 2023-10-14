package com.kabgig.CoinBot.Telegram.service;

import com.kabgig.CoinBot.Telegram.entity.ActiveChat;
import com.kabgig.CoinBot.Telegram.repository.ActiveChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActiveChatService {

    @Autowired
    private static ActiveChatRepository activeChatRepository;

    public static void checkUser(Long userid) {
        if (activeChatRepository.findByChatId(userid) == null) {
            ActiveChat activeChat = new ActiveChat();
            activeChat.setChatId(userid);
            activeChatRepository.save(activeChat);
        }
    }
}
