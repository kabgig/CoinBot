package com.kabgig.CoinBot.Telegram.service;

import com.kabgig.CoinBot.Telegram.entity.ActiveChat;
import com.kabgig.CoinBot.Telegram.repository.ActiveChatRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActiveChatService {
    @Autowired
    private ActiveChatRepository activeChatRepository;

    public void checkUser(Long userid) {
        if (activeChatRepository.findByChatId(userid) == null) {
            ActiveChat activeChat = new ActiveChat();
            activeChat.setChatId(userid);
            activeChat.setNotifications(true);
            activeChatRepository.save(activeChat);
        }
    }

    public List<Long> getUniqueUsersChatIds() {
        return activeChatRepository.findDistinctChatId();
    }

    public List<ActiveChat> getUniqueUsersChats() {
        return activeChatRepository.findAll();
    }

    @Transactional
    public void setNotificationsOn(Long chatId) {
        activeChatRepository.setNotificationsToTrueByChatId(chatId);
    }

    @Transactional
    public void setNotificationsOff(Long chatId) {
        activeChatRepository.setNotificationsToFalseByChatId(chatId);
    }
}
