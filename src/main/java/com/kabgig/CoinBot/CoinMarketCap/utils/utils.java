package com.kabgig.CoinBot.CoinMarketCap.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.kabgig.CoinBot.CoinMarketCap.entity.CurrentData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.kabgig.CoinBot.Utils.Logger.lgr;

public class utils {
    public static LocalDateTime convertToLDT(String inputString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        // Parse the input String to LocalDateTime using the specified format
        return LocalDateTime.parse(inputString, formatter);

    }
    public static String convertTagsArray(JsonArray tagsArray) {
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

    public static void saveToLogFile(List<CurrentData> currentDataList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            writer.write(currentDataList.toString());
            System.out.println("currentDataList IS WRITTEN TO THE LOG FILE: output.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        lgr().info("SAVED RESPONSE TO FILE: output.txt");
    }
}
