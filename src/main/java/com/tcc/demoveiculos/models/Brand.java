package com.tcc.demoveiculos.models;

import com.tcc.demoveiculos.models.dtos.BrandDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "brand")
@Getter
@Setter
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;

    private String urlPathName;

    private String imageUrl;

    @Enumerated(EnumType.ORDINAL)
    private VehicleType vehicleType;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Model> models = new ArrayList<>();

    @Override
    public String toString() {
        return "Brand: " + this.name;
    }

    public static BrandDTO mapToBrandDTO(Brand brand) {
        return new BrandDTO(brand.getId(),
                            brand.getName(),
                            brand.getUrlPathName(),
                            brand.getVehicleType().ordinal(),
                            brand.getImageUrl());
    }
}
