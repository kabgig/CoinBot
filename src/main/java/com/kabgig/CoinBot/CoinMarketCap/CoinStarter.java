package com.kabgig.CoinBot.CoinMarketCap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CoinStarter {
    @Autowired
    private CoinMarketCapService coinMarketCapService;
    @Bean
    public void printCoins(){
        coinMarketCapService.getCoinNameAndPrice();
    }
}
