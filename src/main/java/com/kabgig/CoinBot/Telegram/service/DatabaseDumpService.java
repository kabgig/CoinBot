package com.kabgig.CoinBot.Telegram.service;

import com.kabgig.CoinBot.CoinMarketCap.entity.UserCoins;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class DatabaseDumpService {
    public String dumpTable(List<UserCoins> userCoinsList) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("UserCoins");

            // Create a header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("id");
            headerRow.createCell(1).setCellValue("chat_id");
            headerRow.createCell(2).setCellValue("coin_id");
            headerRow.createCell(3).setCellValue("amount");

            // Create data rows for UserCoins entities
            int rowNum = 1;
            for (UserCoins userCoins : userCoinsList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(userCoins.getId());
                row.createCell(1).setCellValue(userCoins.getChatId());
                row.createCell(2).setCellValue(userCoins.getCoinId());
                row.createCell(3).setCellValue(userCoins.getAmount());
            }

            // Write the Excel file
            try (FileOutputStream outputStream = new FileOutputStream("sqlBackups/userCoins.xlsx")) {
                workbook.write(outputStream);
            }

            System.out.println("SQL IS BACKED UP TO EXCEL FILE.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "SQL is backed up to file";
    }
}