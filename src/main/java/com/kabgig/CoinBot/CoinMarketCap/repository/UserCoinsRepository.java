package com.kabgig.CoinBot.CoinMarketCap.repository;

import com.kabgig.CoinBot.CoinMarketCap.entity.UserCoins;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCoinsRepository extends JpaRepository<UserCoins, Long> {
    List<UserCoins> findByChatId(Long chatId);
    Optional<UserCoins> findByCoinId(Long coinId);
    void deleteByCoinIdAndChatId(Long coinId, Long chatId);
    Optional<UserCoins> findByChatIdAndCoinId(Long chatId, Long coinId);
}
