package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.VehicleType;
import com.tcc.demoveiculos.models.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface YearRepository extends JpaRepository<Year, UUID> {

    List<Year> findByModel_Brand_VehicleType(VehicleType vehicleType);
}
