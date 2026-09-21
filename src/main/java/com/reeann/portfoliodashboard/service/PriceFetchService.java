package com.reeann.portfoliodashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reeann.portfoliodashboard.model.Asset;
import com.reeann.portfoliodashboard.model.AssetType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class PriceFetchService {

    private static final String DEFAULT_CRYPTO_API_URL = "https://api.coingecko.com/api/v3/simple/price";
    private static final String DEFAULT_STOCK_API_URL = "https://query1.finance.yahoo.com/v8/finance/chart/%s";
    private static final String CURRENCY = "usd";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String cryptoApiUrl;
    private final String stockApiUrl;

    public PriceFetchService(
            @Value("${portfolio.pricefetch.crypto-api-url:" + DEFAULT_CRYPTO_API_URL + "}") String cryptoApiUrl,
            @Value("${portfolio.pricefetch.stock-api-url:" + DEFAULT_STOCK_API_URL + "}") String stockApiUrl) {
        this.cryptoApiUrl = cryptoApiUrl;
        this.stockApiUrl = stockApiUrl;
    }

    public double fetchPrice(Asset asset) {
        if (asset.getType() == AssetType.CRYPTO) {
            return fetchCryptoPrice(asset.getApiId());
        }
        return fetchStockPrice(asset.getApiId());
    }

    private double fetchCryptoPrice(String coinId) {
        String url = cryptoApiUrl + "?ids=" + encode(coinId) + "&vs_currencies=" + CURRENCY;
        JsonNode root = get(url);
        JsonNode priceNode = root.path(coinId).path(CURRENCY);
        if (priceNode.isMissingNode()) {
            throw new PriceFetchException("No price returned for crypto id '" + coinId + "'");
        }
        return priceNode.asDouble();
    }

    private double fetchStockPrice(String symbol) {
        String url = String.format(stockApiUrl, encode(symbol));
        JsonNode root = get(url);
        JsonNode priceNode = root.path("chart").path("result").path(0).path("meta").path("regularMarketPrice");
        if (priceNode.isMissingNode()) {
            throw new PriceFetchException("No price returned for symbol '" + symbol + "'");
        }
        return priceNode.asDouble();
    }

    private JsonNode get(String url) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "portfolio-dashboard")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new PriceFetchException("Request to " + url + " failed with status " + response.statusCode());
            }
            return objectMapper.readTree(response.body());
        } catch (PriceFetchException e) {
            throw e;
        } catch (Exception e) {
            throw new PriceFetchException("Failed to fetch price from " + url, e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
