package com.example.kitchenpalapi.dto;

import lombok.Data;
import java.util.List;

@Data
public class ScanResult {
    private List<DetectedIngredient> ingredients;
    private List<Long> suggestedRecipeIds;

    @Data
    public static class DetectedIngredient {
        private String name;
        private Integer confidence;
        private String weight;
        private String imageUrl;
    }
}
