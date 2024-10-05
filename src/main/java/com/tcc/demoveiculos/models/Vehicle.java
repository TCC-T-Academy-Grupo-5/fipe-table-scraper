package com.tcc.demoveiculos.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "vehicle")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String fipeCode;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vehicle")
    private List<FipePrice> fipePrices;

    @OneToOne
    @JoinColumn(name = "year_id")
    private Year year;
}
