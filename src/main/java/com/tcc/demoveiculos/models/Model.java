package com.tcc.demoveiculos.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "model")
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String name;

    private String urlPathName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    @JsonIgnore
    private Brand brand;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Year> years = new ArrayList<>();

    @Override
    public String toString() {
        return "Model{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", urlPathName='" + urlPathName + '\'' +
                ", brand=" + brand +
                '}';
    }
}
