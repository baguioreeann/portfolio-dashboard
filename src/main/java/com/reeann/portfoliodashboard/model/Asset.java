package com.reeann.portfoliodashboard.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AssetType type;

    @NotBlank
    private String apiId;

    @NotBlank
    private String label;

    private Double currentPrice;

    private Double previousPrice;

    private Instant lastChecked;

    protected Asset() {
    }

    public Asset(AssetType type, String apiId, String label) {
        this.type = type;
        this.apiId = apiId;
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public AssetType getType() {
        return type;
    }

    public void setType(AssetType type) {
        this.type = type;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public Double getPreviousPrice() {
        return previousPrice;
    }

    public Instant getLastChecked() {
        return lastChecked;
    }

    public void recordPrice(double price, Instant checkedAt) {
        this.previousPrice = this.currentPrice;
        this.currentPrice = price;
        this.lastChecked = checkedAt;
    }

    public Double getChangePct() {
        if (currentPrice == null || previousPrice == null || previousPrice == 0) {
            return null;
        }
        return (currentPrice - previousPrice) / previousPrice * 100;
    }
}
