package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, String> {
}
