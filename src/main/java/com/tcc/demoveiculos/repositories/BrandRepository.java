package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.Brand;
import com.tcc.demoveiculos.models.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, String> {
    long countAllByVehicleType(VehicleType vehicleType);
}
