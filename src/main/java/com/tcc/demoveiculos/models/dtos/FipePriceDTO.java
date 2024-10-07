package com.tcc.demoveiculos.models.dtos;

import java.util.UUID;

public record FipePriceDTO(UUID id, Integer month, Integer year, Double price, UUID version_id) {
}
