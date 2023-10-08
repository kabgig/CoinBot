package com.kabgig.CoinBot.CoinMarketCap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.kabgig.CoinBot.Utils.Logger.lgr;

@Service
public class CoinMarketCapService {

    private static String apiKey = "b54bcf4d-1bca-4e8e-9a24-22ff2c3d462c";

    public String getCoinNameAndPrice(){
        String jsonResponse = getCoinData();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            writer.write(jsonResponse);
            System.out.println("String written to the file.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        lgr().info("STARTED JSON STUFF");

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
            result = "Name: " + name + "Symbol: " + symbol + "Price: " + price;
        }
        return result;
    }
    public static String getCoinData() {
        String uri = "https://sandbox-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("start","1"));
        parameters.add(new BasicNameValuePair("limit","3"));
        parameters.add(new BasicNameValuePair("convert","USD"));
        String result = "";
        try {
            result = makeAPICall(uri, parameters);
        } catch (IOException e) {
            System.out.println("Error: can't access content - " + e.toString());
        } catch (URISyntaxException e) {
            System.out.println("Error: Invalid URL " + e.toString());
        }
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

}

