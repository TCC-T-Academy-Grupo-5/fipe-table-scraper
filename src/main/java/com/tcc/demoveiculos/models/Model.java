package com.tcc.demoveiculos.models;

import com.tcc.demoveiculos.models.dtos.ModelDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "model")
@Getter
@Setter
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String urlPathName;

    private String imageUrl;

    private ModelCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Year> years = new ArrayList<>();

    @Override
    public String toString() {
        return "Model: " + this.name;
    }

    public static ModelDTO mapToModelDTO(Model model) {
        return new ModelDTO(model.getId(),
                            model.getImageUrl(),
                            model.getName(),
                            model.getUrlPathName(),
                            model.getBrand().getId());
    }
}
