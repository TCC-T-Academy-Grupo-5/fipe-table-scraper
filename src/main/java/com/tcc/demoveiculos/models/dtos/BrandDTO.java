package com.tcc.demoveiculos.models.dtos;

import java.util.UUID;

public record BrandDTO(UUID id, String name, String url_path_name, Integer vehicle_type, String image_url) {
}
