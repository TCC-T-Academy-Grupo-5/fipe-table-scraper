package com.tcc.demoveiculos.modelsv3;

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

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ModelV3> models = new ArrayList<>();

    @Override
    public String toString() {
        return "Brand: " + this.name;
    }
}
