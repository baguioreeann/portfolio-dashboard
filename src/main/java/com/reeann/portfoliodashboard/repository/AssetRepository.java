package com.reeann.portfoliodashboard.repository;

import com.reeann.portfoliodashboard.model.Asset;
import com.reeann.portfoliodashboard.model.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByTypeOrderByLabel(AssetType type);
}
