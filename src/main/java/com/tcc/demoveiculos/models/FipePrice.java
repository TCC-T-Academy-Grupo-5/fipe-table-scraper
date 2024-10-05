package com.tcc.demoveiculos.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
@Table(name = "fipe_price")
public class FipePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String price;

    @ManyToOne
    @JoinColumn(name = "fipe_month_reference_id")
    @JsonIgnore
    private FipeMonthReference fipeMonthReference;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    @JsonIgnore
    private Vehicle vehicle;
}
