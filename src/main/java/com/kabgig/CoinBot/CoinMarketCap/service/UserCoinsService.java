package com.kabgig.CoinBot.CoinMarketCap.service;

import com.kabgig.CoinBot.CoinMarketCap.entity.UserCoins;
import com.kabgig.CoinBot.CoinMarketCap.repository.UserCoinsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserCoinsService {
    @Autowired
    private UserCoinsRepository userCoinsRepository;

    public UserCoins addCoinSubscribtion(Long chatId, Long coinId, double amountOfCrypto) {
        UserCoins userCoin = new UserCoins();
        userCoin.setCoinId(coinId);
        userCoin.setChatId(chatId);
        userCoin.setAmount(amountOfCrypto);
        return userCoinsRepository.save(userCoin);
    }

    public List<Long> getDistinctCoinIds() {
        return userCoinsRepository
                .findAll()
                .stream()
                .map(userCoinEntity -> userCoinEntity.getCoinId())
                .distinct()
                .collect(Collectors.toList());
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

    public Optional<UserCoins> getOneUserCoinByChatIdAndCoinId(Long chatId, Long coinId) {
        return userCoinsRepository.findByChatIdAndCoinId(chatId, coinId);
    }

    public double getCryptoAmount(Long chatId, Long coinId) {
        return getOneUserCoinByChatIdAndCoinId(chatId, coinId)
                .get()
                .getAmount();
    }

    public List<UserCoins> getAllUserCoins() {
        return userCoinsRepository.findAll();
    }
}
