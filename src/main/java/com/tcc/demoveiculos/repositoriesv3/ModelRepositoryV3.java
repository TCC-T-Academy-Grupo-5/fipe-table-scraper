package com.tcc.demoveiculos.repositoriesv3;

import com.tcc.demoveiculos.modelsv3.ModelV3;
import com.tcc.demoveiculos.modelsv3.VehicleTypeV3;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModelRepositoryV3 extends JpaRepository<ModelV3, UUID> {

    List<ModelV3> findAllByBrand_VehicleType(VehicleTypeV3 vehicleTypeV3);
}
