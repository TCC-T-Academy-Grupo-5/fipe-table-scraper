package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.Model;
import com.tcc.demoveiculos.models.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModelRepository extends JpaRepository<Model, UUID> {

    List<Model> findAllByBrand_VehicleType(VehicleType vehicleType);

    List<Model> findAllByCategoryStringIsNotNull();
}
