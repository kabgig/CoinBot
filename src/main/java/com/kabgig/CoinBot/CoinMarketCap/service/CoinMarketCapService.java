package com.kabgig.CoinBot.CoinMarketCap.service;

import com.google.gson.*;
import com.kabgig.CoinBot.CoinMarketCap.entity.CurrentData;
import com.kabgig.CoinBot.CoinMarketCap.repository.CurrentDataRepository;
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
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.kabgig.CoinBot.Utils.Logger.lgr;

@Service
public class CoinMarketCapService {

    @Autowired
    private CurrentDataRepository currentDataRepository;

    private static String apiKey = "b54bcf4d-1bca-4e8e-9a24-22ff2c3d462c";
    private static String uri = "https://sandbox-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";

    public String getCoinNameAndPrice() {
        String jsonResponse = getCoinData();
        lgr().info("GOT JSON RESPONSE");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            writer.write(jsonResponse);
            System.out.println("String written to the file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        lgr().info("SAVED RESPONSE TO FILE: output.txt");

        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray data = jsonObject.getAsJsonArray("data");
        String result = "";
        for (int i = 0; i < data.size(); i++) {
            JsonElement jsonElement = data.get(i);
            String name = jsonElement.getAsJsonObject().get("name").getAsString();
            String symbol = jsonElement.getAsJsonObject().get("symbol").getAsString();
            String price = jsonElement
                    .getAsJsonObject().get("quote")
                    .getAsJsonObject().get("USD")
                    .getAsJsonObject().get("price").getAsString();
            System.out.println("Name: " + name);
            System.out.println("Symbol: " + symbol);
            System.out.println("Price: " + price);
            System.out.println();
            result = "Name: " + name + "\nSymbol: " + symbol + "\nPrice: " + price;
        }
        return result;
    }

    public String getCoinData() {
        String result = "";
        //check if the date is NOT current,
        if (true) {
            //fetch data from coinmarketcap
            try {
                result = makeAPICall(uri, getParameters());
                lgr().info("DID makeAPICall and got result " + result);
                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                JsonArray data = jsonObject.getAsJsonArray("data");
                lgr().info("PROCESSED DATA ARRAY AND READY FOR MARSHALLING: " + data);
                try {
                    List<CurrentData> currentDataArray = mapToEntityArray(data);
                    lgr().info("STARTING SAVING DATA TO REPOSITORY");
                    // Iterate through the array and save each entity to the database
                    for (CurrentData currentData : currentDataArray) {
                        lgr().info("STARTING SAVING ENTITY: " + currentData);
                        currentDataRepository.save(currentData);
                        System.out.println(currentData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                System.out.println("Error: can't access content - " + e.toString());
            } catch (URISyntaxException e) {
                System.out.println("Error: Invalid URL " + e.toString());
            }
        } else {
            // skip fetching and request db currentData
        }
        return result;
    }

    private List<CurrentData> mapToEntityArray(JsonArray data) {
        List<CurrentData> result = new ArrayList<>();
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
            lgr().info("ADDED CURRENT DATA TO LIST: " + currentData);

        }
        return result;
    }

    private LocalDateTime convertToLDT(String inputString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        // Parse the input String to LocalDateTime using the specified format
        return LocalDateTime.parse(inputString, formatter);

    }

    public static String makeAPICall(String uri, List<NameValuePair> parameters)
            throws URISyntaxException, IOException {
        String response_content = "";

        URIBuilder query = new URIBuilder(uri);
        query.addParameters(parameters);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(query.build());

        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader("X-CMC_PRO_API_KEY", apiKey);

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

    private String convertTagsArray(JsonArray tagsArray) {
        StringBuilder tagsStringBuilder = new StringBuilder();
        for (JsonElement tagElement : tagsArray) {
            String tag = tagElement.getAsString();
            tagsStringBuilder.append(tag).append(",");
        }
        if (tagsStringBuilder.length() > 0) {
            tagsStringBuilder.setLength(tagsStringBuilder.length() - 1);
        }
        return tagsStringBuilder.toString();
    }
}

