package com.tcc.demoveiculos.models;

import com.tcc.demoveiculos.models.dtos.VersionDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "version")
@Getter
@Setter
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String fipeCode;

    private String urlPathName;

    private String fullUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "year_id")
    private Year year;

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FipePrice> fipePrices = new ArrayList<>();

    public static VersionDTO mapToVersionDTO(Version version) {
        return new VersionDTO(version.getId(),
                              version.getName(),
                              version.getUrlPathName(),
                              version.getFipeCode(),
                              version.getFullUrl(),
                              version.getYear().getId());
    }
}