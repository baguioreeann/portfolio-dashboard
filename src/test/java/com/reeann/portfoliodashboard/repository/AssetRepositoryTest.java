package com.reeann.portfoliodashboard.repository;

import com.reeann.portfoliodashboard.model.Asset;
import com.reeann.portfoliodashboard.model.AssetType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class AssetRepositoryTest {

    @Autowired
    private AssetRepository assetRepository;

    @Test
    void findByTypeOrderByLabelReturnsOnlyMatchingTypeSortedByLabel() {
        assetRepository.save(new Asset(AssetType.STOCK, "MSFT", "Microsoft"));
        assetRepository.save(new Asset(AssetType.STOCK, "AAPL", "Apple"));
        assetRepository.save(new Asset(AssetType.CRYPTO, "bitcoin", "Bitcoin"));

        List<Asset> stocks = assetRepository.findByTypeOrderByLabel(AssetType.STOCK);

        assertThat(stocks).extracting(Asset::getLabel).containsExactly("Apple", "Microsoft");
    }

    @Test
    void findByTypeOrderByLabelReturnsEmptyListWhenNoneMatch() {
        assetRepository.save(new Asset(AssetType.CRYPTO, "bitcoin", "Bitcoin"));

        List<Asset> etfs = assetRepository.findByTypeOrderByLabel(AssetType.ETF);

        assertThat(etfs).isEmpty();
    }
}
