package com.tcc.demoveiculos.models.dtos;

import java.util.UUID;

public record VersionDTO(UUID id, String name, String url_path_name, String fipe_code, String full_url, UUID year_id) {
}
