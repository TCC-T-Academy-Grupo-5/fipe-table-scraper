package com.tcc.demoveiculos.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "brand")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String name;

    private String urlPathName;

    @Enumerated(EnumType.ORDINAL)
    private VehicleType vehicleType;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Model> models = new ArrayList<>();

    @Override
    public String toString() {
        return "Brand{" +
                "vehicleType=" + vehicleType +
                ", urlPathName='" + urlPathName + '\'' +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
