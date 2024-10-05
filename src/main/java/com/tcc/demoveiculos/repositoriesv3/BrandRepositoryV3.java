package com.tcc.demoveiculos.repositoriesv3;

import com.tcc.demoveiculos.modelsv3.BrandV3;
import com.tcc.demoveiculos.modelsv3.VehicleTypeV3;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BrandRepositoryV3 extends JpaRepository<BrandV3, UUID> {

    List<BrandV3> findAllByVehicleType(VehicleTypeV3 vehicleType);
}
