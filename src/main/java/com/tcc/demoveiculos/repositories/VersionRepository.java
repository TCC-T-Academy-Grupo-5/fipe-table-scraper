package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.Version;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VersionRepository extends JpaRepository<Version, UUID> {
}
