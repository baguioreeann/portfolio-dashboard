package com.reeann.portfoliodashboard.service;

import com.reeann.portfoliodashboard.model.Asset;
import com.reeann.portfoliodashboard.model.AssetType;
import com.reeann.portfoliodashboard.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private PriceFetchService priceFetchService;

    private AssetService assetService;

    private AssetService service() {
        return new AssetService(assetRepository, priceFetchService);
    }

    @Test
    void findByTypeDelegatesToRepository() {
        assetService = service();
        List<Asset> stocks = List.of(new Asset(AssetType.STOCK, "AAPL", "Apple"));
        when(assetRepository.findByTypeOrderByLabel(AssetType.STOCK)).thenReturn(stocks);

        List<Asset> result = assetService.findByType(AssetType.STOCK);

        assertThat(result).isEqualTo(stocks);
    }

    @Test
    void addAssetTrimsApiIdAndLabelBeforeSaving() {
        assetService = service();
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asset saved = assetService.addAsset(AssetType.CRYPTO, "  bitcoin  ", "  Bitcoin  ");

        assertThat(saved.getApiId()).isEqualTo("bitcoin");
        assertThat(saved.getLabel()).isEqualTo("Bitcoin");
        assertThat(saved.getType()).isEqualTo(AssetType.CRYPTO);
    }

    @Test
    void deleteAssetDelegatesToRepository() {
        assetService = service();

        assetService.deleteAsset(42L);

        verify(assetRepository).deleteById(42L);
    }

    @Test
    void refreshAllUpdatesEveryAssetOnSuccess() throws Exception {
        assetService = service();
        Asset bitcoin = new Asset(AssetType.CRYPTO, "bitcoin", "Bitcoin");
        Asset apple = new Asset(AssetType.STOCK, "AAPL", "Apple");
        when(assetRepository.findAll()).thenReturn(List.of(bitcoin, apple));
        when(priceFetchService.fetchPrice(bitcoin)).thenReturn(50000.0);
        when(priceFetchService.fetchPrice(apple)).thenReturn(190.0);

        RefreshResult result = assetService.refreshAll();

        assertThat(result.updated()).isEqualTo(2);
        assertThat(result.errors()).isEmpty();
        assertThat(bitcoin.getCurrentPrice()).isEqualTo(50000.0);
        assertThat(apple.getCurrentPrice()).isEqualTo(190.0);
    }

    @Test
    void refreshAllCollectsErrorsAndSkipsFailedAssets() {
        assetService = service();
        Asset bitcoin = new Asset(AssetType.CRYPTO, "bitcoin", "Bitcoin");
        Asset badAsset = new Asset(AssetType.STOCK, "BAD", "Broken");
        when(assetRepository.findAll()).thenReturn(List.of(bitcoin, badAsset));
        when(priceFetchService.fetchPrice(bitcoin)).thenReturn(50000.0);
        when(priceFetchService.fetchPrice(badAsset)).thenThrow(new PriceFetchException("no price"));

        RefreshResult result = assetService.refreshAll();

        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.errors()).containsExactly("Broken: no price");
        assertThat(bitcoin.getCurrentPrice()).isEqualTo(50000.0);
        assertThat(badAsset.getCurrentPrice()).isNull();
    }

    @Test
    void refreshAllWithNoAssetsReturnsEmptyResult() {
        assetService = service();
        when(assetRepository.findAll()).thenReturn(List.of());

        RefreshResult result = assetService.refreshAll();

        assertThat(result.updated()).isZero();
        assertThat(result.errors()).isEmpty();
        verify(priceFetchService, never()).fetchPrice(any());
    }
}
