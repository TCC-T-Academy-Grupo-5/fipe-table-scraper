package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.Brand;
import com.tcc.demoveiculos.models.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {

    List<Brand> findAllByVehicleType(VehicleType vehicleType);
}
