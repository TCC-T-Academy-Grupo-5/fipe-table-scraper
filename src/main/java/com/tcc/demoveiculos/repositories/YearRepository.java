package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.Year;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YearRepository extends JpaRepository<Year, String> {
}
