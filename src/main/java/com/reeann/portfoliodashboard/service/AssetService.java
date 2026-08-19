package com.reeann.portfoliodashboard.service;

import com.reeann.portfoliodashboard.model.Asset;
import com.reeann.portfoliodashboard.model.AssetType;
import com.reeann.portfoliodashboard.repository.AssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final PriceFetchService priceFetchService;

    public AssetService(AssetRepository assetRepository, PriceFetchService priceFetchService) {
        this.assetRepository = assetRepository;
        this.priceFetchService = priceFetchService;
    }

    public List<Asset> findByType(AssetType type) {
        return assetRepository.findByTypeOrderByLabel(type);
    }

    public Asset addAsset(AssetType type, String apiId, String label) {
        return assetRepository.save(new Asset(type, apiId.trim(), label.trim()));
    }

    public void deleteAsset(Long id) {
        assetRepository.deleteById(id);
    }

    @Transactional
    public RefreshResult refreshAll() {
        List<Asset> assets = assetRepository.findAll();
        List<String> errors = new ArrayList<>();
        int updated = 0;

        for (Asset asset : assets) {
            try {
                double price = priceFetchService.fetchPrice(asset);
                asset.recordPrice(price, Instant.now());
                updated++;
            } catch (Exception e) {
                errors.add(asset.getLabel() + ": " + e.getMessage());
            }
        }

        return new RefreshResult(updated, errors);
    }
}
