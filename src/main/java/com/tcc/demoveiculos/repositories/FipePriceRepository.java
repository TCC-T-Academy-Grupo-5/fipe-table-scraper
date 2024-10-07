package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.FipePrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FipePriceRepository extends JpaRepository<FipePrice, UUID> {
}
