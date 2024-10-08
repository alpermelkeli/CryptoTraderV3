package com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance;

import com.alpermelkeli.cryptotrader.model.view.Coin;
import com.alpermelkeli.cryptotrader.model.view.Trade;
import com.alpermelkeli.cryptotrader.repository.cryptoApi.AccountOperations;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

public class BinanceAccountOperations implements AccountOperations{
    private static final String API_URL = "https://api.binance.com/api/v3/account";
    private static final String BASE_URL = "https://api.binance.com/api";

    private String API_KEY;
    private String API_SECRET;
    public BinanceAccountOperations(String apiKey, String apiSecret) {
        this.API_KEY = apiKey;
        this.API_SECRET = apiSecret;
    }

    public String getAPI_KEY() {
        return API_KEY;
    }

    public void setAPI_KEY(String API_KEY) {
        this.API_KEY = API_KEY;
    }

    public String getAPI_SECRET() {
        return API_SECRET;
    }

    public void setAPI_SECRET(String API_SECRET) {
        this.API_SECRET = API_SECRET;
    }

    private final OkHttpClient client = new OkHttpClient();

    private String generateSignature(String data) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(API_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKeySpec);
        return bytesToHex(hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String encode(String value) throws Exception {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    @Override
    public CompletableFuture<Double> getAccountBalance() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long timestamp = System.currentTimeMillis();
                String queryString = "timestamp=" + timestamp;
                String signature = generateSignature(queryString);
                queryString += "&signature=" + encode(signature);

                HttpUrl httpUrl = HttpUrl.parse(API_URL).newBuilder().encodedQuery(queryString).build();

                Request request = new Request.Builder()
                        .url(httpUrl)
                        .addHeader("X-MBX-APIKEY", API_KEY)
                        .build();


                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray balances = json.getJSONArray("balances");
                    double totalBalance = 0.0;
                    for (int i = 0; i < balances.length(); i++) {
                        JSONObject balance = balances.getJSONObject(i);
                        String asset = balance.getString("asset");
                        double free = balance.getDouble("free");
                        double locked = balance.getDouble("locked");

                        if (asset.equals("USDT")) {
                            totalBalance += free + locked;
                        } else if (free + locked != 0) {
                            totalBalance += convertToUSDT(balance.getString("asset"), balance.getDouble("free") + balance.getDouble("locked"));
                        }
                    }
                    return totalBalance;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                return 0.0;
            }
        });
    }
    @Override
    public CompletableFuture<List<Coin>> getAccountWallet() {
        return CompletableFuture.supplyAsync(() -> {
            List<Coin> coinWallet = new ArrayList<>();
            try {
                long timestamp = System.currentTimeMillis();
                String queryString = "timestamp=" + timestamp;
                String signature = generateSignature(queryString);
                queryString += "&signature=" + encode(signature);

                HttpUrl httpUrl = HttpUrl.parse(API_URL)
                        .newBuilder()
                        .encodedQuery(queryString)
                        .build();

                Request request = new Request.Builder()
                        .url(httpUrl)
                        .addHeader("X-MBX-APIKEY", API_KEY)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        System.out.println("Request failed: " + response);
                        throw new IOException("Unexpected code " + response);
                    }

                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray balances = json.getJSONArray("balances");

                    for (int i = 0; i < balances.length(); i++) {
                        JSONObject balanceJson = balances.getJSONObject(i);
                        String coinName = balanceJson.getString("asset");
                        double quantity = balanceJson.getDouble("free");
                        double locked = balanceJson.getDouble("locked");

                        if (quantity > 0 || locked > 0) {
                            double totalQuantity = quantity + locked;
                            Coin coin = new Coin(coinName, totalQuantity, convertToUSDT(coinName, totalQuantity));
                            coinWallet.add(coin);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            return coinWallet;
        });
    }
    private double convertToUSDT(String asset, double amount) {
        try {
            if(asset.equals("USDT")){return amount;}
            // API endpoint to get the price of the asset in USDT
            String tickerPriceUrl = "https://api.binance.com/api/v3/ticker/price?symbol=" + asset + "USDT";

            Request request = new Request.Builder()
                    .url(tickerPriceUrl)
                    .addHeader("X-MBX-APIKEY", API_KEY)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                JSONObject priceJson = new JSONObject(responseBody);
                double price = priceJson.getDouble("price");

                // Return the amount in USDT
                return amount * price;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
    private double convertToQuantity(String asset, double usdAmount){
        try {
            String tickerPriceUrl = "https://api.binance.com/api/v3/ticker/price?symbol=" + asset;

            Request request = new Request.Builder()
                    .url(tickerPriceUrl)
                    .addHeader("X-MBX-APIKEY", API_KEY)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                JSONObject priceJson = new JSONObject(responseBody);
                double price = priceJson.getDouble("price");

                // Return the amount in USDT
                return usdAmount / price;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
    private double adjustQuantityToLotSize(String symbol, double quantity) {
        try {
            String exchangeInfoUrl = BASE_URL + "/v3/exchangeInfo?symbol=" + symbol;

            Request request = new Request.Builder()
                    .url(exchangeInfoUrl)
                    .addHeader("X-MBX-APIKEY", API_KEY)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                JSONObject exchangeInfoJson = new JSONObject(responseBody);
                JSONObject symbolInfo = exchangeInfoJson.getJSONArray("symbols").getJSONObject(0);
                JSONArray filters = symbolInfo.getJSONArray("filters");

                for (int i = 0; i < filters.length(); i++) {
                    JSONObject filter = filters.getJSONObject(i);
                    if (filter.getString("filterType").equals("LOT_SIZE")) {
                        double stepSize = filter.getDouble("stepSize");
                        double minQty = filter.getDouble("minQty");

                        // Adjust the quantity to be a multiple of stepSize and not less than minQty
                        double adjustedQuantity = Math.max(Math.floor(quantity / stepSize) * stepSize, minQty);
                        return adjustedQuantity;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return 0.0;
    }
    @Override
    public CompletableFuture<Double> getSelectedCoinQuantity(String asset) {
        return CompletableFuture.supplyAsync(() -> {

            try {
                long timestamp = System.currentTimeMillis();
                String queryString = "timestamp=" + timestamp;
                String signature = generateSignature(queryString);
                queryString += "&signature=" + encode(signature);

                HttpUrl httpUrl = HttpUrl.parse(API_URL).newBuilder().encodedQuery(queryString).build();

                Request request = new Request.Builder()
                        .url(httpUrl)
                        .addHeader("X-MBX-APIKEY", API_KEY)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray balances = json.getJSONArray("balances");

                    for (int i = 0; i < balances.length(); i++) {
                        JSONObject balance = balances.getJSONObject(i);
                        if (balance.getString("asset").equalsIgnoreCase(asset)) {
                            double free = balance.getDouble("free");
                            double locked = balance.getDouble("locked");
                            return free + locked;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            return 0.0;
        });
    }
    @Override
    public CompletableFuture<List<Trade>> getTradeHistorySelectedCoin(String pairName) {
        return CompletableFuture.supplyAsync(() -> {
            List<Trade> tradeHistory = new ArrayList<>();
            try {
                long timestamp = System.currentTimeMillis();
                String queryString = "symbol=" + pairName + "&timestamp=" + timestamp;
                String signature = generateSignature(queryString);
                queryString += "&signature=" + encode(signature);

                HttpUrl httpUrl = HttpUrl.parse(BASE_URL+"/v3/myTrades")
                        .newBuilder()
                        .encodedQuery(queryString)
                        .build();

                Request request = new Request.Builder()
                        .url(httpUrl)
                        .addHeader("X-MBX-APIKEY", API_KEY)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        System.out.println("Request failed: " + response);
                        throw new IOException("Unexpected code " + response);
                    }

                    String responseBody = response.body().string();

                    JSONArray json = new JSONArray(responseBody);

                    for (int i = 0; i < json.length(); i++) {
                        JSONObject tradeJson = json.getJSONObject(i);
                        long timeMillis = tradeJson.getLong("time");
                        double price = tradeJson.getDouble("price");
                        double amount = tradeJson.getDouble("qty");
                        boolean isBuyer = tradeJson.getBoolean("isBuyer");
                        boolean isBestMatch = tradeJson.getBoolean("isBestMatch");
                        String time = convertMillisToDate(timeMillis).split(" ")[1];
                        if(isBestMatch){
                            Trade trade = new Trade(time, price, amount,isBuyer);
                            tradeHistory.add(trade);
                        }

                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            Collections.reverse(tradeHistory);
            return tradeHistory;
        });
    }
    @Override
    public CompletableFuture<TradeResult> buyCoin(String symbol, double quantity) {
        CompletableFuture<TradeResult> future = new CompletableFuture<>();
        try {
            String endpoint = "/v3/order";
            String url = BASE_URL + endpoint;

            long timestamp = System.currentTimeMillis();
            String queryString = "symbol=" + encode(symbol) + "&side=BUY&type=MARKET&quantity=" + quantity + "&timestamp=" + timestamp;
            String signature = generateSignature(queryString);
            queryString += "&signature=" + encode(signature);

            HttpUrl httpUrl = HttpUrl.parse(url).newBuilder().encodedQuery(queryString).build();

            Request request = new Request.Builder()
                    .url(httpUrl)
                    .addHeader("X-MBX-APIKEY", API_KEY)
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    future.complete(new TradeResult(false, 0.0));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try{
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            JSONObject json = new JSONObject(responseBody);
                            JSONArray fills = json.getJSONArray("fills");
                            double totalCommission = 0.0;

                            for (int i = 0; i < fills.length(); i++) {
                                JSONObject fill = fills.getJSONObject(i);
                                double commission = fill.getDouble("commission");
                                totalCommission += commission;
                            }

                            System.out.println("Buy order placed successfully");
                            future.complete(new TradeResult(true, totalCommission));
                        } else {
                            System.out.println("Failed to place buy order: " + response.code() + " | " + response.message() + " | " + response.body().string());
                            future.complete(new TradeResult(false, 0.0));
                        }
                    }
                    catch (Exception e){
                        System.out.printf(e.getMessage());
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            future.complete(new TradeResult(false, 0.0));
        }

        return future;
    }
    @Override
    public CompletableFuture<TradeResult> buyCoinWithUSDT(String symbol, double usdtQuantity) {
        CompletableFuture<TradeResult> future = new CompletableFuture<>();
        try {
            String endpoint = "/v3/order";

            String url = BASE_URL + endpoint;

            double quantity = convertToQuantity(symbol,usdtQuantity);

            quantity = adjustQuantityToLotSize(symbol,quantity);


            long timestamp = System.currentTimeMillis();

            String queryString = "symbol=" + encode(symbol) + "&side=BUY&type=MARKET&quantity=" + quantity + "&timestamp=" + timestamp;

            String signature = generateSignature(queryString);

            queryString += "&signature=" + encode(signature);

            System.out.println(queryString);

            HttpUrl httpUrl = HttpUrl.parse(url).newBuilder().encodedQuery(queryString).build();

            Request request = new Request.Builder()
                    .url(httpUrl)
                    .addHeader("X-MBX-APIKEY", API_KEY)
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    future.complete(new TradeResult(false, 0.0));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try{
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            JSONObject json = new JSONObject(responseBody);
                            JSONArray fills = json.getJSONArray("fills");
                            double totalCommission = 0.0;

                            for (int i = 0; i < fills.length(); i++) {
                                JSONObject fill = fills.getJSONObject(i);
                                double commission = fill.getDouble("commission");
                                totalCommission += commission;
                            }

                            System.out.println("Buy order placed successfully");
                            future.complete(new TradeResult(true, totalCommission));
                        } else {
                            System.out.println("Failed to place buy order: " + response.code() + " | " + response.message() + " | " + response.body().string());
                            future.complete(new TradeResult(false, 0.0));
                        }
                    }
                    catch (Exception e){
                        System.out.printf(e.getMessage());
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            future.complete(new TradeResult(false, 0.0));
        }

        return future;
    }

    @Override
    public CompletableFuture<TradeResult> sellCoin(String symbol, double quantity) {
        CompletableFuture<TradeResult> future = new CompletableFuture<>();

        try {
            String endpoint = "/v3/order";
            String url = BASE_URL + endpoint;

            long timestamp = System.currentTimeMillis();
            String queryString = "symbol=" + encode(symbol) + "&side=SELL&type=MARKET&quantity=" + quantity + "&timestamp=" + timestamp;
            String signature = generateSignature(queryString);
            queryString += "&signature=" + encode(signature);

            HttpUrl httpUrl = HttpUrl.parse(url).newBuilder().encodedQuery(queryString).build();

            Request request = new Request.Builder()
                    .url(httpUrl)
                    .addHeader("X-MBX-APIKEY", API_KEY)
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    future.complete(new TradeResult(false, 0.0));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try{
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            JSONObject json = new JSONObject(responseBody);
                            JSONArray fills = json.getJSONArray("fills");
                            double totalCommission = 0.0;

                            for (int i = 0; i < fills.length(); i++) {
                                JSONObject fill = fills.getJSONObject(i);
                                double commission = fill.getDouble("commission");
                                totalCommission += commission;
                            }

                            System.out.println("Sell order placed successfully");
                            future.complete(new TradeResult(true, totalCommission));
                        } else {
                            System.out.println("Failed to place sell order: " + response.code() + " | " + response.message() + " | " + response.body().string());
                            future.complete(new TradeResult(false, 0.0));
                        }
                    }
                    catch (Exception e){
                        System.out.printf(e.getMessage());
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            future.complete(new TradeResult(false, 0.0));
        }

        return future;
    }
    @Override
    public CompletableFuture<TradeResult> sellCoinWithUSDT(String symbol, double usdtQuantity) {
        CompletableFuture<TradeResult> future = new CompletableFuture<>();

        try {
            String endpoint = "/v3/order";
            String url = BASE_URL + endpoint;
            double quantity = convertToQuantity(symbol,usdtQuantity);
            quantity = adjustQuantityToLotSize(symbol,quantity);
            long timestamp = System.currentTimeMillis();
            String queryString = "symbol=" + encode(symbol) + "&side=SELL&type=MARKET&quantity=" + quantity + "&timestamp=" + timestamp;
            String signature = generateSignature(queryString);
            queryString += "&signature=" + encode(signature);

            HttpUrl httpUrl = HttpUrl.parse(url).newBuilder().encodedQuery(queryString).build();

            Request request = new Request.Builder()
                    .url(httpUrl)
                    .addHeader("X-MBX-APIKEY", API_KEY)
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    future.complete(new TradeResult(false, 0.0));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try{
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            JSONObject json = new JSONObject(responseBody);
                            JSONArray fills = json.getJSONArray("fills");
                            double totalCommission = 0.0;

                            for (int i = 0; i < fills.length(); i++) {
                                JSONObject fill = fills.getJSONObject(i);
                                double commission = fill.getDouble("commission");
                                totalCommission += commission;
                            }

                            System.out.println("Sell order placed successfully");
                            future.complete(new TradeResult(true, totalCommission));
                        } else {
                            System.out.println("Failed to place sell order: " + response.code() + " | " + response.message() + " | " + response.body().string());
                            future.complete(new TradeResult(false, 0.0));
                        }
                    }
                    catch (Exception e){
                        System.out.printf(e.getMessage());
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            future.complete(new TradeResult(false, 0.0));
        }

        return future;
    }

    private String convertMillisToDate(long millis) {
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+3:00"));
        return sdf.format(date);
    }
    @Override
    public CompletableFuture<Boolean> openLongToCoin(String symbol, double quantity, int leverage) {
        return null;
    }
    @Override
    public CompletableFuture<Boolean> openShortToCoin(String symbol, double quantity, int leverage) {
        return null;
    }
}





