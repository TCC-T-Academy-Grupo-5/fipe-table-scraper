package com.tcc.demoveiculos.repositoriesv3;

import com.tcc.demoveiculos.modelsv3.BrandV3;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BrandRepositoryV3 extends JpaRepository<BrandV3, UUID> {
}
