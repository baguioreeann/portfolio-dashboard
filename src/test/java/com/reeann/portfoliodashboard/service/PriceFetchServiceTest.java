package com.reeann.portfoliodashboard.service;

import com.reeann.portfoliodashboard.model.Asset;
import com.reeann.portfoliodashboard.model.AssetType;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceFetchServiceTest {

    private HttpServer server;
    private PriceFetchService priceFetchService;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        priceFetchService = new PriceFetchService(baseUrl + "/simple/price", baseUrl + "/chart/%s");
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private void respond(String path, int status, String body) {
        server.createContext(path, exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
    }

    @Test
    void fetchesCryptoPriceFromCoinGeckoStyleResponse() {
        respond("/simple/price", 200, "{\"bitcoin\":{\"usd\":50000.5}}");

        double price = priceFetchService.fetchPrice(new Asset(AssetType.CRYPTO, "bitcoin", "Bitcoin"));

        assertThat(price).isEqualTo(50000.5);
    }

    @Test
    void fetchesStockPriceFromYahooStyleResponse() {
        respond("/chart/AAPL", 200,
                "{\"chart\":{\"result\":[{\"meta\":{\"regularMarketPrice\":123.45}}]}}");

        double price = priceFetchService.fetchPrice(new Asset(AssetType.STOCK, "AAPL", "Apple"));

        assertThat(price).isEqualTo(123.45);
    }

    @Test
    void throwsWhenCryptoResponseHasNoPriceForCoin() {
        respond("/simple/price", 200, "{}");

        assertThatThrownBy(() -> priceFetchService.fetchPrice(new Asset(AssetType.CRYPTO, "bitcoin", "Bitcoin")))
                .isInstanceOf(PriceFetchException.class)
                .hasMessageContaining("bitcoin");
    }

    @Test
    void throwsWhenStockResponseHasNoResult() {
        respond("/chart/BAD", 200, "{\"chart\":{\"result\":[]}}");

        assertThatThrownBy(() -> priceFetchService.fetchPrice(new Asset(AssetType.STOCK, "BAD", "Broken")))
                .isInstanceOf(PriceFetchException.class)
                .hasMessageContaining("BAD");
    }

    @Test
    void throwsWhenServerReturnsNon200Status() {
        respond("/simple/price", 500, "internal error");

        assertThatThrownBy(() -> priceFetchService.fetchPrice(new Asset(AssetType.CRYPTO, "bitcoin", "Bitcoin")))
                .isInstanceOf(PriceFetchException.class)
                .hasMessageContaining("500");
    }
}
