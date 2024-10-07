package com.tcc.demoveiculos.models;

import com.tcc.demoveiculos.models.dtos.YearDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "year")
@Getter
@Setter
public class Year {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String urlPathName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "model_id")
    private Model model;

    @OneToMany(mappedBy = "year", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Version> versions = new ArrayList<>();

    @Override
    public String toString() {
        return "Year: " + this.name;
    }

    public static YearDTO mapToYearDTO(Year year) {
        return new YearDTO(year.getId(), year.getName(), year.getUrlPathName(), year.getModel().getId());
    }
}
