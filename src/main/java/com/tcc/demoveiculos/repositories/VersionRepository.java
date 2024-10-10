package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VersionRepository extends JpaRepository<Version, UUID> {

//    @Query(value = "SELECT v.* FROM version v JOIN year y ON v.year_id = y.id JOIN model m ON y.model_id = m.id WHERE m.id = :modelId LIMIT 1", nativeQuery = true)
//    Version findFirstVersionByModelId(@Param("modelId") String modelId);

    @Query(value = "SELECT v.* FROM version v " +
            "JOIN (SELECT DISTINCT ON (y.model_id) y.model_id, v.id " +
            "FROM year y JOIN version v ON y.id = v.year_id " +
            "WHERE y.model_id IN :modelIds " +
            "ORDER BY y.model_id, RANDOM()) as subquery " +
            "ON v.id = subquery.id", nativeQuery = true)
    List<Version> findRandomVersionsByModelIds(@Param("modelIds") List<String> modelIds);
}
