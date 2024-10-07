package com.tcc.demoveiculos.models.dtos;

import java.util.UUID;

public record ModelDTO(String id, String image_url, String name, String url_path_name, UUID brand_id) {
}
