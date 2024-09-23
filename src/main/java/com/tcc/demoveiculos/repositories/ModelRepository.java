package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.Model;
import com.tcc.demoveiculos.models.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelRepository extends JpaRepository<Model, String> {
    List<Model> findAllByBrand_VehicleType(VehicleType vehicleType);
}
