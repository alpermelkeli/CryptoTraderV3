package com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class BinancePumpDetection {
    WebSocketClient webSocketClient;

    private KlineListener klineListener;

    public void setKlineListener(KlineListener listener) {
        this.klineListener = listener;
    }

    public void startWebSocketConnection() {
        String[] tradingPairs = {
                "btcusdt","ethusdt","bnbusdt","neousdt","ltcusdt","qtumusdt","adausdt","xrpusdt","eosusdt","tusdusdt","iotausdt","xlmusdt","ontusdt","trxusdt","etcusdt","icxusdt","nulsusdt","vetusdt","usdcusdt","linkusdt","wavesusdt","ongusdt","hotusdt","zilusdt","zrxusdt","fetusdt","batusdt","xmrusdt","zecusdt","iostusdt","celrusdt","dashusdt","omgusdt","thetausdt","enjusdt","maticusdt","atomusdt","tfuelusdt","oneusdt","ftmusdt","algousdt","dogeusdt","duskusdt","ankrusdt","winusdt","cosusdt","mtlusdt","tomousdt","perlusdt","dentusdt","keyusdt","dockusdt","wanusdt","funusdt","cvcusdt","chzusdt","bandusdt","busdusdt","xtzusdt","renusdt","rvnusdt","hbarusdt","nknusdt","stxusdt","kavausdt","arpausdt","iotxusdt","rlcusdt","ctxcusdt","bchusdt","troyusdt","viteusdt","eurusdt","ognusdt","drepusdt","bullusdt","bearusdt","ethbullusdt","ethbearusdt","tctusdt","wrxusdt","btsusdt","lskusdt","bntusdt","ltousdt","stratusdt","aionusdt","mblusdt","cotiusdt","stptusdt","wtcusdt","datausdt","xzcusdt","solusdt","ctsiusdt","hiveusdt","chrusdt","btcupusdt","btcdownusdt","gxsusdt","ardrusdt","lendusdt","mdtusdt","stmxusdt","kncusdt","repusdt","lrcusdt","pntusdt","compusdt","bkrwusdt","scusdt","zenusdt","snxusdt","ethupusdt","ethdownusdt","adaupusdt","adadownusdt","linkupusdt","linkdownusdt","vthousdt","dgbusdt","gbpusdt","sxpusdt","mkrusdt","dcrusdt","storjusdt","manausdt","yfiusdt","balusdt","blzusdt","irisusdt","kmdusdt","jstusdt","antusdt","crvusdt","sandusdt","oceanusdt","nmrusdt","dotusdt","lunausdt","rsrusdt","paxgusdt","wnxmusdt","trbusdt","sushiusdt","umausdt","belusdt","wingusdt","uniusdt","oxtusdt","sunusdt","avaxusdt","flmusdt","ornusdt","utkusdt","xvsusdt","alphausdt","aaveusdt","nearusdt","filusdt","injusdt","audiousdt","ctkusdt","akrousdt","axsusdt","hardusdt","straxusdt","unfiusdt","roseusdt","avausdt","xemusdt","sklusdt","grtusdt","juvusdt","psgusdt","1inchusdt","reefusdt","ogusdt","atmusdt","asrusdt","celousdt","rifusdt","truusdt","ckbusdt","twtusdt","firousdt","litusdt","sfpusdt","dodousdt","cakeusdt","acmusdt","badgerusdt","fisusdt","omusdt","pondusdt","degousdt","aliceusdt","linausdt","perpusdt","superusdt","cfxusdt","tkousdt","pundixusdt","tlmusdt","barusdt","forthusdt","bakeusdt","burgerusdt","slpusdt","shibusdt","icpusdt","arusdt","polsusdt","mdxusdt","maskusdt","lptusdt","xvgusdt","atausdt","gtcusdt","ernusdt","klayusdt","phausdt","bondusdt","mlnusdt","dexeusdt","c98usdt","clvusdt","qntusdt","flowusdt","tvkusdt","minausdt","rayusdt","farmusdt","alpacausdt","quickusdt","mboxusdt","forusdt","requsdt","ghstusdt","waxpusdt","gnousdt","xecusdt","elfusdt","dydxusdt","idexusdt","vidtusdt","usdpusdt","galausdt","ilvusdt","yggusdt","sysusdt","dfusdt","fidausdt","frontusdt","cvpusdt","agldusdt","radusdt","betausdt","rareusdt","laziousdt","chessusdt","adxusdt","auctionusdt","darusdt","bnxusdt","movrusdt","cityusdt","ensusdt","kp3rusdt","qiusdt","portousdt","powrusdt","vgxusdt","jasmyusdt","ampusdt","plausdt","pyrusdt","rndrusdt","alcxusdt","santosusdt","mcusdt","bicousdt","fluxusdt","fxsusdt","voxelusdt","highusdt","cvxusdt","peopleusdt","ookiusdt","spellusdt","ustusdt","joeusdt","achusdt","imxusdt","glmrusdt","lokausdt","scrtusdt","api3usdt","bttcusdt","acausdt","xnousdt","woousdt","alpineusdt","tusdt","astrusdt","gmtusdt","kdausdt","apeusdt","bswusdt","bifiusdt","multiusdt","steemusdt","mobusdt","nexousdt","reiusdt","galusdt","ldousdt","epxusdt","opusdt","leverusdt","stgusdt","luncusdt","gmxusdt","polyxusdt","aptusdt","osmousdt","hftusdt","phbusdt","hookusdt","magicusdt","hifiusdt","rplusdt","prosusdt","agixusdt","gnsusdt","synusdt","vibusdt","ssvusdt","lqtyusdt","ambusdt","bethusdt","gasusdt","glmusdt","promusdt","qkcusdt","uftusdt","idusdt","arbusdt","loomusdt","oaxusdt","rdntusdt","wbtcusdt","eduusdt","suiusdt","aergousdt","pepeusdt","flokiusdt","astusdt","sntusdt","combousdt","mavusdt","pendleusdt","arkmusdt","wbethusdt","wldusdt","fdusdusd"
        };

        Draft[] drafts = {new Draft_6455()};
        try {
            webSocketClient = new WebSocketClient(new URI("wss://stream.binance.com:9443/ws"), drafts[0]) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d("WebSocket", "Opened");
                    subscribeToKlines(tradingPairs);
                }

                @Override
                public void onMessage(String message) {
                    Log.d("WebSocket", "Received: " + message);
                    processKline(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d("WebSocket", "Closed");
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private void subscribeToKlines(String[] tradingPairs) {
        StringBuilder subscribeMessageBuilder = new StringBuilder("{\"method\": \"SUBSCRIBE\",\"params\":[");
        for (int i = 0; i < tradingPairs.length; i++) {
            if (i > 0) {
                subscribeMessageBuilder.append(",");
            }
            String pair = tradingPairs[i];
            subscribeMessageBuilder.append("\"").append(pair).append("@kline_" + "5m" + "\"");
        }
        subscribeMessageBuilder.append("],\"id\": 1}");
        String subscribeMessage = subscribeMessageBuilder.toString();
        webSocketClient.send(subscribeMessage);
    }
    private void processKline(String message) {
        try {
            JSONObject json = new JSONObject(message);
            if (json.has("s") && json.has("k")) {
                JSONObject klineData = json.getJSONObject("k");
                String symbol = json.getString("s");
                double openPrice = klineData.getDouble("o");
                double closePrice = klineData.getDouble("c");
                double priceChange = closePrice - openPrice;
                double percentChange = (priceChange / openPrice) * 100;
                Log.d("WebSocket", "Symbol: " + symbol + ", Price Change: " + priceChange + ", Percent Change: " + percentChange);

                if (klineListener != null) {
                    klineListener.onKlineProcessed(symbol, percentChange, closePrice);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void  stopWebSocketConnection() {
        webSocketClient.close(1000, null);
    }

}
