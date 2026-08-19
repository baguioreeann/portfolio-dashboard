package com.reeann.portfoliodashboard.web;

import com.reeann.portfoliodashboard.model.Asset;
import com.reeann.portfoliodashboard.model.AssetType;
import com.reeann.portfoliodashboard.service.AssetService;
import com.reeann.portfoliodashboard.service.RefreshResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final AssetService assetService;

    public DashboardController(AssetService assetService) {
        this.assetService = assetService;
    }

    @ModelAttribute("assetTypes")
    public AssetType[] assetTypes() {
        return AssetType.values();
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        Map<AssetType, List<Asset>> byType = Map.of(
                AssetType.STOCK, assetService.findByType(AssetType.STOCK),
                AssetType.CRYPTO, assetService.findByType(AssetType.CRYPTO),
                AssetType.ETF, assetService.findByType(AssetType.ETF)
        );
        List<AssetType> sectionOrder = List.of(AssetType.STOCK, AssetType.CRYPTO, AssetType.ETF);

        model.addAttribute("sectionOrder", sectionOrder);
        model.addAttribute("byType", byType);
        return "dashboard";
    }

    @PostMapping("/assets")
    public String addAsset(@RequestParam AssetType type,
                            @RequestParam String apiId,
                            @RequestParam String label,
                            RedirectAttributes redirectAttributes) {
        assetService.addAsset(type, apiId, label);
        redirectAttributes.addFlashAttribute("message", "Added " + label + ".");
        return "redirect:/";
    }

    @PostMapping("/assets/{id}/delete")
    public String deleteAsset(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        assetService.deleteAsset(id);
        redirectAttributes.addFlashAttribute("message", "Asset removed.");
        return "redirect:/";
    }

    @PostMapping("/refresh")
    public String refresh(RedirectAttributes redirectAttributes) {
        RefreshResult result = assetService.refreshAll();
        String message = "Refreshed " + result.updated() + " asset(s).";
        if (!result.errors().isEmpty()) {
            message += " Errors: " + result.errors().stream().collect(Collectors.joining("; "));
        }
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/";
    }
}
