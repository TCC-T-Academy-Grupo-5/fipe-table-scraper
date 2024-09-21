package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.Brand;
import com.tcc.demoveiculos.models.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, String> {
    List<Brand> findAllByVehicleType(VehicleType vehicleType);
}
