package com.kabgig.CoinBot.CoinMarketCap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class CurrentData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String symbol;
    private String slug;
    private int cmcRank;
    private int numMarketPairs;
    private double circulatingSupply;
    private double totalSupply;
    private double maxSupply;
    private boolean infiniteSupply;
    private LocalDateTime lastUpdated;
    private LocalDateTime dateAdded;


    private String tags;

    private double usd_price;
    private double usd_volume24h;
    private double usd_volumeChange24h;
    private double usd_percentChange1h;
    private double usd_percentChange24h;
    private double usd_percentChange7d;
    private double usd_marketCap;
    private int usd_marketCapDominance;
    private double usd_fullyDilutedMarketCap;
    private LocalDateTime usd_lastUpdated;

//    private double btc_price;
//    private double btc_volume24h;
//    private double btc_volumeChange24h;
//    private double btc_percentChange1h;
//    private double btc_percentChange24h;
//    private double btc_percentChange7d;
//    private double btc_marketCap;
//    private int btc_marketCapDominance;
//    private double btc_fullyDilutedMarketCap;
//    private String btc_lastUpdated;
}

