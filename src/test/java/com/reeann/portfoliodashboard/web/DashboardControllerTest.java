package com.reeann.portfoliodashboard.web;

import com.reeann.portfoliodashboard.model.Asset;
import com.reeann.portfoliodashboard.model.AssetType;
import com.reeann.portfoliodashboard.service.AssetService;
import com.reeann.portfoliodashboard.service.RefreshResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssetService assetService;

    @Test
    void dashboardRendersAssetsGroupedByType() throws Exception {
        when(assetService.findByType(AssetType.STOCK))
                .thenReturn(List.of(new Asset(AssetType.STOCK, "AAPL", "Apple")));
        when(assetService.findByType(AssetType.CRYPTO)).thenReturn(List.of());
        when(assetService.findByType(AssetType.ETF)).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("byType"))
                .andExpect(model().attribute("sectionOrder", List.of(AssetType.STOCK, AssetType.CRYPTO, AssetType.ETF)));
    }

    @Test
    void addAssetDelegatesToServiceAndRedirects() throws Exception {
        mockMvc.perform(post("/assets")
                        .param("type", "CRYPTO")
                        .param("apiId", "bitcoin")
                        .param("label", "Bitcoin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message", "Added Bitcoin."));

        verify(assetService).addAsset(AssetType.CRYPTO, "bitcoin", "Bitcoin");
    }

    @Test
    void deleteAssetDelegatesToServiceAndRedirects() throws Exception {
        mockMvc.perform(post("/assets/{id}/delete", 7L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message", "Asset removed."));

        verify(assetService).deleteAsset(7L);
    }

    @Test
    void refreshWithNoErrorsShowsUpdatedCount() throws Exception {
        when(assetService.refreshAll()).thenReturn(new RefreshResult(3, List.of()));

        mockMvc.perform(post("/refresh"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message", "Refreshed 3 asset(s)."));
    }

    @Test
    void refreshWithErrorsAppendsErrorMessages() throws Exception {
        when(assetService.refreshAll())
                .thenReturn(new RefreshResult(1, List.of("Bitcoin: timeout", "AAPL: no price")));

        mockMvc.perform(post("/refresh"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message",
                        "Refreshed 1 asset(s). Errors: Bitcoin: timeout; AAPL: no price"));
    }
}
