package com.kabgig.CoinBot.CoinMarketCap.repository;

import com.kabgig.CoinBot.CoinMarketCap.entity.CurrentData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrentDataRepository extends JpaRepository<CurrentData,Long> {
}
