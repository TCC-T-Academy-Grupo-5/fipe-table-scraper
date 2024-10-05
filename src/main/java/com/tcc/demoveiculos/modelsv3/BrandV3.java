package com.tcc.demoveiculos.modelsv3;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "brand")
@Getter
@Setter
public class BrandV3 {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;

    private String urlPathName;

    private String imageUrl;

    @Enumerated(EnumType.ORDINAL)
    private VehicleTypeV3 vehicleType;

    @Override
    public String toString() {
        return "Brand: " + this.name;
    }
}
