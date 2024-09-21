package com.tcc.demoveiculos.repositories;

import com.tcc.demoveiculos.models.Model;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, String> {
}
