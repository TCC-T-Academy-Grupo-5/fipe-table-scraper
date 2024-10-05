package com.tcc.demoveiculos.modelsv3;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "model")
@Getter
@Setter
public class ModelV3 {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String urlPathName;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private BrandV3 brand;

    @Override
    public String toString() {
        return "Model: " + this.name;
    }
}
