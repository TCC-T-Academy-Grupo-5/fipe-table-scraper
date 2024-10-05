package com.tcc.demoveiculos.modelsv3;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private BrandV3 brand;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<YearV3> years = new ArrayList<>();

    @Override
    public String toString() {
        return "Model: " + this.name;
    }
}
