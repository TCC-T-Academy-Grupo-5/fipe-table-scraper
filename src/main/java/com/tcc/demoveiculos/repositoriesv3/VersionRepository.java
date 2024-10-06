package com.tcc.demoveiculos.repositoriesv3;

import com.tcc.demoveiculos.modelsv3.Version;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VersionRepository extends JpaRepository<Version, UUID> {
}
