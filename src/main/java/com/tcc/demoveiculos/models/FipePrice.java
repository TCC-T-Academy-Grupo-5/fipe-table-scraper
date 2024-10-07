package com.tcc.demoveiculos.models;

import com.tcc.demoveiculos.models.dtos.FipePriceDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "fipe_price")
@Getter
@Setter
public class FipePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Integer month;

    private Integer year;

    private Double price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "version_id")
    private Version version;

    public static FipePriceDTO mapToFipePriceDTO(FipePrice fipePrice) {
        return new FipePriceDTO(fipePrice.getId(),
                                fipePrice.getMonth(),
                                fipePrice.getYear(),
                                fipePrice.getPrice(),
                                fipePrice.getVersion().getId());
    }
}
