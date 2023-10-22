package com.kabgig.CoinBot.CoinMarketCap.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kabgig.CoinBot.CoinMarketCap.entity.CurrentData;
import com.kabgig.CoinBot.CoinMarketCap.entity.UserCoins;
import com.kabgig.CoinBot.CoinMarketCap.repository.CurrentDataRepository;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.kabgig.CoinBot.CoinMarketCap.utils.utils.*;
import static com.kabgig.CoinBot.Utils.Logger.lgr;

@Service
@Data
public class CoinMarketCapService {

    @Autowired
    private CurrentDataRepository currentDataRepository;

    private static String apiKeyDummy = "b54bcf4d-1bca-4e8e-9a24-22ff2c3d462c";
    private static String apiKeyReal = "8f2300f8-ebae-4bcb-9e8e-4405c0fbeb2e";
    private static String uriLatest = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
    private List<CurrentData> currentDataArray = null;
    private List<CurrentData> currentSavingArray = null;
    private volatile boolean updateInProgress;

    public boolean isCurrentdate() {
        boolean isCurrent;
        Optional<CurrentData> byId = currentDataRepository.findById(1L);
        if (byId.isEmpty()) {
            lgr().info("THE DATE IS NOT CURRENT");
            return false;
        }
        var last = byId.get().getLastUpdated();
        var now = LocalDateTime.now();
        if (now.getMonth() == last.getMonth() &&
                now.getDayOfMonth() == last.getDayOfMonth()) {
            isCurrent = true;
            lgr().info("THE DATE IS CURRENT");
        } else {
            lgr().info("THE DATE IS NOT CURRENT");
            isCurrent = false;
        }
        return isCurrent;
    }

    private List<CurrentData> mapToEntityArray(JsonArray data) {
        lgr().info("STARTED ADDING ENTITIES TO THE LIST");
        List<CurrentData> result = new ArrayList<>();
        int count = 1;
        for (JsonElement element : data) {
            CurrentData currentData = new CurrentData();
            currentData.setId(element.getAsJsonObject().get("id").getAsLong());
            currentData.setName(element.getAsJsonObject().get("name").getAsString());
            currentData.setSymbol(element.getAsJsonObject().get("symbol").getAsString());
            currentData.setSlug(element.getAsJsonObject().get("slug").getAsString());
            currentData.setCmcRank(element.getAsJsonObject().get("cmc_rank").getAsInt());
            currentData.setNumMarketPairs(element.getAsJsonObject().get("num_market_pairs").getAsInt());
            currentData.setCirculatingSupply(element.getAsJsonObject().get("circulating_supply").getAsDouble());
            currentData.setTotalSupply(element.getAsJsonObject().get("total_supply").getAsDouble());

            JsonElement infinite_supply = element.getAsJsonObject().get("infinite_supply");
            if (!infinite_supply.isJsonNull())
                currentData.setInfiniteSupply(infinite_supply.getAsBoolean());

            currentData.setLastUpdated(convertToLDT(element.getAsJsonObject().get("last_updated").getAsString()));
            currentData.setDateAdded(convertToLDT(element.getAsJsonObject().get("date_added").getAsString()));

            JsonArray tagsArray = element.getAsJsonObject().get("tags").getAsJsonArray();
            String tags = convertTagsArray(tagsArray);

            JsonObject quoteObject = element.getAsJsonObject().get("quote").getAsJsonObject();
            JsonObject usdObject = quoteObject.getAsJsonObject("USD");
            //JsonObject btcObject = quoteObject.getAsJsonObject("BTC");

// Extract values from the USD part
            currentData.setUsd_price(usdObject.get("price").getAsDouble());
            currentData.setUsd_volume24h(usdObject.get("volume_24h").getAsDouble());
            currentData.setUsd_volumeChange24h(usdObject.get("volume_change_24h").getAsDouble());
            currentData.setUsd_percentChange1h(usdObject.get("percent_change_1h").getAsDouble());
            currentData.setUsd_percentChange24h(usdObject.get("percent_change_24h").getAsDouble());
            currentData.setUsd_percentChange7d(usdObject.get("percent_change_7d").getAsDouble());
            currentData.setUsd_marketCap(usdObject.get("market_cap").getAsDouble());
            currentData.setUsd_marketCapDominance(usdObject.get("market_cap_dominance").getAsInt());
            currentData.setUsd_fullyDilutedMarketCap(usdObject.get("fully_diluted_market_cap").getAsDouble());
            currentData.setUsd_lastUpdated(convertToLDT(usdObject.get("last_updated").getAsString()));
// Extract values from the BTC part
//            currentData.setBtc_price(btcObject.get("price").getAsDouble());
//            currentData.setBtc_volume24h(btcObject.get("volume_24h").getAsDouble());
//            currentData.setBtc_volumeChange24h(btcObject.get("volume_change_24h").getAsDouble());
//            currentData.setBtc_percentChange1h(btcObject.get("percent_change_1h").getAsDouble());
//            currentData.setBtc_percentChange24h(btcObject.get("percent_change_24h").getAsDouble());
//            currentData.setBtc_percentChange7d(btcObject.get("percent_change_7d").getAsDouble());
//            currentData.setBtc_marketCap(btcObject.get("market_cap").getAsDouble());
//            currentData.setBtc_marketCapDominance(btcObject.get("market_cap_dominance").getAsInt());
//            currentData.setBtc_fullyDilutedMarketCap(btcObject.get("fully_diluted_market_cap").getAsDouble());
//            currentData.setBtc_lastUpdated(btcObject.get("last_updated").getAsString());

            result.add(currentData);
            count++;
        }
        lgr().info("ADDED " + count + " entities to list");
        return result;
    }

    public static String makeAPICall(String uri, List<NameValuePair> parameters)
            throws URISyntaxException, IOException {
        String response_content = "";

        URIBuilder query = new URIBuilder(uri);
        query.addParameters(parameters);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(query.build());

        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader("X-CMC_PRO_API_KEY", apiKeyReal);

        CloseableHttpResponse response = client.execute(request);

        try {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            response_content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }

        return response_content;
    }

    private List<NameValuePair> getParameters() {
        List<NameValuePair> paratmers = new ArrayList<NameValuePair>();
        paratmers.add(new BasicNameValuePair("start", "1"));
        paratmers.add(new BasicNameValuePair("limit", "5000"));
        paratmers.add(new BasicNameValuePair("convert", "USD"));
        return paratmers;
    }

    public List<String> getCoinSymbolList() {
        List<String> symbols = new ArrayList<>();
        List<CurrentData> all = currentDataRepository.findAll();
        for (var i : all) {
            symbols.add(i.getSymbol());
        }
        return symbols;
    }

    public CurrentData getOneCoinData(String cmd) {
        return currentDataRepository.findBySymbol(cmd);
    }

    public List<CurrentData> getCustomCoinList(List<UserCoins> userCoins) {
        List<Long> coinIds = new ArrayList<>();
        for (var item : userCoins)
            coinIds.add(item.getCoinId());

        List<CurrentData> coinList = new ArrayList<>();
        List<CurrentData> all = currentDataRepository.findAll();
        for (var currentData : all) {
            if (coinIds.contains(currentData.getId()))
                coinList.add(currentData);
        }
        return coinList;
    }

    public CurrentData getOneCoinDataById(Long coinId) {
        return currentDataRepository.findById(coinId).get();
    }

    public CurrentData getOneCoinBySymbol(String coinSymbol) {
        return currentDataRepository.findBySymbol(coinSymbol);
    }

    @Async
    public String updateDatabase() {
        updateInProgress = true;
        lgr().info("UPDATE IN PROGRESS: " + updateInProgress + " THREAD: " + Thread.currentThread().getName());
        LocalDateTime start = LocalDateTime.now();
        int N = 1;
        int count = 0;
        try {
            int batchSize = 1000;
            List<CurrentData> batchToSave = new ArrayList<>(batchSize);
            String result = makeAPICall(uriLatest, getParameters());
            lgr().info("EXECUTED makeApiCall() AND GOT RESULT");
            JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
            JsonArray data = jsonObject.getAsJsonArray("data");
            lgr().info("PROCESSED DATA ARRAY AND READY FOR MARSHALLING");
            try {
                currentDataArray = mapToEntityArray(data);
                currentSavingArray = currentDataArray;
                lgr().info("STARTING SAVING DATA TO REPOSITORY");
                int countSize = currentSavingArray.size();
                for (CurrentData currentData : currentSavingArray) {
                    batchToSave.add(currentData);
                    N++;
                    if (batchToSave.size() == batchSize || (countSize - count) == batchToSave.size()) {
                        currentDataRepository.saveAll(batchToSave);
                        count += batchToSave.size();
                        lgr().info("SAVED " + batchToSave.size() + " entities, total = " + count);
                        batchToSave.clear();
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
                return e.toString();
            }
        } catch (IOException e) {
            System.out.println("Error: can't access content - " + e);
            return e.toString();
        } catch (URISyntaxException e) {
            System.out.println("Error: Invalid URL " + e);
            return e.toString();
        }
        Duration duration = Duration.between(start, LocalDateTime.now());
        updateInProgress = false;
        lgr().info("UPDATE IN PROGRESS: " + updateInProgress + " THREAD: " + Thread.currentThread().getName());
        return "Executed daily database refresh, processed " +
                count + " entities during " +
                duration.toMinutes() + " minutes " +
                duration.toSeconds() + " seconds";
    }
}

