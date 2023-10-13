package com.kabgig.CoinBot.CoinMarketCap;

import com.kabgig.CoinBot.CoinMarketCap.service.CoinMarketCapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

//@Component
public class CoinStarter {
    @Autowired
    private CoinMarketCapService coinMarketCapService;
    @Bean
    public void printCoins(){
        coinMarketCapService.getCoinNameAndPrice();
    }
}
