package com.kabgig.CoinBot.CoinMarketCap.service;

import com.kabgig.CoinBot.CoinMarketCap.entity.UserCoins;
import com.kabgig.CoinBot.CoinMarketCap.repository.UserCoinsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserCoinsService {
    @Autowired
    private UserCoinsRepository userCoinsRepository;

    public UserCoins addCoinSubscribtion(Long chatId, Long coinId){
        UserCoins userCoin = new UserCoins();
        userCoin.setCoinId(coinId);
        userCoin.setChatId(chatId);
        return userCoinsRepository.save(userCoin);
    }

    public List<UserCoins> getUserCoins(Long chatId) {
        return userCoinsRepository.findByChatId(chatId);
    }

    public Optional<UserCoins> getOneUserCoin(Long coinId) {
        return userCoinsRepository.findByCoinId(coinId);
    }

    @Transactional
    public void deleteByCoinAndUserId(Long coinId, Long chatId) {
        userCoinsRepository.deleteByCoinIdAndChatId(coinId, chatId);
    }
}
