package com.tcc.demoveiculos.modelsv3;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "year")
@Getter
@Setter
public class YearV3 {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String urlPathName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "model_id")
    private ModelV3 model;

    @Override
    public String toString() {
        return "Year: " + this.name;
    }
}
