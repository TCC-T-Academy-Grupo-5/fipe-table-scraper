package com.tcc.demoveiculos.repositoriesv3;

import com.tcc.demoveiculos.modelsv3.VehicleTypeV3;
import com.tcc.demoveiculos.modelsv3.YearV3;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface YearRepositoryV3 extends JpaRepository<YearV3, UUID> {

    List<YearV3> findByModel_Brand_VehicleType(VehicleTypeV3 vehicleType);
}
