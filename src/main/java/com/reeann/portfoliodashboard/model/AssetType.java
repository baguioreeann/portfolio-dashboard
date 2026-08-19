package com.reeann.portfoliodashboard.model;

public enum AssetType {
    CRYPTO("Crypto"),
    STOCK("Stocks"),
    ETF("ETFs");

    private final String displayName;

    AssetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
