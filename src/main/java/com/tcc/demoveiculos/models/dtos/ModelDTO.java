package com.tcc.demoveiculos.models.dtos;

import com.tcc.demoveiculos.models.ModelCategory;

import java.util.UUID;

public record ModelDTO(
        String id,
        String image_url,
        String name,
        String url_path_name,
        ModelCategory category,
        String categoryString,
        UUID brand_id
) {
}
