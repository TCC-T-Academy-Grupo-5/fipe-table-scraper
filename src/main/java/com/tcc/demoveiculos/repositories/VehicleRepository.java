package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, String> {
}
