package com.reeann.portfoliodashboard.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AssetTest {

    @Test
    void newAssetHasNoPriceHistory() {
        Asset asset = new Asset(AssetType.CRYPTO, "bitcoin", "Bitcoin");

        assertThat(asset.getCurrentPrice()).isNull();
        assertThat(asset.getPreviousPrice()).isNull();
        assertThat(asset.getLastChecked()).isNull();
        assertThat(asset.getChangePct()).isNull();
    }

    @Test
    void recordPriceSetsCurrentAndShiftsPreviousPrice() {
        Asset asset = new Asset(AssetType.STOCK, "AAPL", "Apple");
        Instant firstCheck = Instant.parse("2024-01-01T00:00:00Z");
        Instant secondCheck = Instant.parse("2024-01-02T00:00:00Z");

        asset.recordPrice(100.0, firstCheck);
        assertThat(asset.getCurrentPrice()).isEqualTo(100.0);
        assertThat(asset.getPreviousPrice()).isNull();
        assertThat(asset.getLastChecked()).isEqualTo(firstCheck);

        asset.recordPrice(110.0, secondCheck);
        assertThat(asset.getCurrentPrice()).isEqualTo(110.0);
        assertThat(asset.getPreviousPrice()).isEqualTo(100.0);
        assertThat(asset.getLastChecked()).isEqualTo(secondCheck);
    }

    @Test
    void changePctIsNullWithoutTwoRecordedPrices() {
        Asset asset = new Asset(AssetType.ETF, "QQQ", "Invesco QQQ");

        assertThat(asset.getChangePct()).isNull();

        asset.recordPrice(50.0, Instant.now());
        assertThat(asset.getChangePct()).isNull();
    }

    @Test
    void changePctIsNullWhenPreviousPriceIsZero() {
        Asset asset = new Asset(AssetType.ETF, "QQQ", "Invesco QQQ");

        asset.recordPrice(0.0, Instant.now());
        asset.recordPrice(10.0, Instant.now());

        assertThat(asset.getChangePct()).isNull();
    }

    @Test
    void changePctComputesPositiveAndNegativeChanges() {
        Asset asset = new Asset(AssetType.STOCK, "AAPL", "Apple");

        asset.recordPrice(100.0, Instant.now());
        asset.recordPrice(110.0, Instant.now());
        assertThat(asset.getChangePct()).isEqualTo(10.0);

        asset.recordPrice(99.0, Instant.now());
        assertThat(asset.getChangePct()).isCloseTo(-10.0, org.assertj.core.data.Offset.offset(0.0001));
    }
}
