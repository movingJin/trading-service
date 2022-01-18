package com.tradingbot.tradingservice.bithumbRestApi.service;

import com.tradingbot.tradingservice.bithumbRestApi.Api_Client;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.tradingbot.tradingservice.bithumbRestApi.HttpRequest.METHOD_POST;

@Slf4j
@Service
public class BithumbApiServiceImpl implements BithumbApiService {

    @Override
    public String marketBidding(String connectKey, String secretKey, String coinName, Double quantity) {
        String order_id = null;
        Api_Client api = new Api_Client(connectKey, secretKey);

        try {
            {   //Buying token by market price
                String reqUrl = "/trade/market_buy";
                HashMap<String, String> rgParams = new HashMap<>();
                rgParams.put("order_currency", coinName);
                rgParams.put("units", "" + quantity);
                rgParams.put("payment_currency", "KRW");
                String result = api.callApi(reqUrl, rgParams, METHOD_POST);
                log.info("testbug: "+ result);
                JSONObject jObject = new JSONObject(result);
                String status = jObject.getString("status");
                if (status.equals("0000")) {
                    order_id = jObject.getString("order_id");
                }
                else
                {
                    log.error("marketBidding method error: " + jObject);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return order_id;
    }

    @Override
    public String marketSell(String connectKey, String secretKey, String coinName, Double quantity) {
        String order_id = null;
        Api_Client api = new Api_Client(connectKey, secretKey);

        try {
            {   //Selling token by market price
                String reqUrl = "/trade/market_sell";
                HashMap<String, String> rgParams = new HashMap<>();
                rgParams.put("order_currency", coinName);
                rgParams.put("units", "" + quantity);
                rgParams.put("payment_currency", "KRW");
                String result = api.callApi(reqUrl, rgParams, METHOD_POST);
                log.info("testbug: "+ result);
                JSONObject jObject = new JSONObject(result);
                String status = jObject.getString("status");
                if (status.equals("0000")) {
                    order_id = jObject.getString("order_id");
                }
                else
                {
                    log.error("marketSell method error: " + jObject);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return order_id;
    }

    @Override
    public Double getBalance(String connectKey, String secretKey)
    {
        Double balance = null;
        Api_Client api = new Api_Client(connectKey, secretKey);
        try
        {   //Getting and setting quantity
            String reqUrl = String.format("/info/balance");
            HashMap<String, String> rgParams = new HashMap<>();
            rgParams.put("currency", "BTC");
            String result = api.callApi(reqUrl, rgParams, METHOD_POST);
            JSONObject jObject = new JSONObject(result);
            String status = jObject.getString("status");
            if (status.equals("0000")) {
                JSONObject dataObject = jObject.getJSONObject("data");
                balance = Double.parseDouble(dataObject.getString("available_krw"));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return balance;
    }
}
