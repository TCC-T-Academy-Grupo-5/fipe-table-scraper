package com.tcc.demoveiculos.models;

public enum ModelCategory {
    COMPACT_SEDAN("Sedã compacto"),
    SUBCOMPACT_HATCH("Hatch subcompacto"),
    MEDIUM_CONVERTIBLE("Conversível médio"),
    LARGE_SEDAN("Sedã grande"),
    LARGE_SUV("SUV grande"),
    COMPACT_SPORTS("Esportivo compacto"),
    COMPACT_VAN("Furgão compacto"),
    COMPACT_SUV("SUV compacto"),
    MEDIUM_VAN("Van média"),
    URBAN_TRUCK("Caminhão urbano"),
    BUGGY("Buggy"),
    MEDIUM_FAMILY("Familiar médio"),
    MEDIUM_HATCH("Hatch médio"),
    LARGE_PICKUP("Picape grande"),
    MEDIUM_SUV("SUV médio"),
    LARGE_FAMILY("Familiar grande"),
    COMPACT_ADVENTURER("Aventureiro compacto"),
    COMPACT_HATCH("Hatch compacto"),
    COMPACT_FAMILY("Familiar compacto"),
    LARGE_VAN("Van grande"),
    ELECTRIC("Elétrico"),
    MEDIUM_PICKUP("Picape média"),
    LARGE_CONVERTIBLE("Conversível grande"),
    JEEP("Jipe"),
    MEDIUM_SPORTS("Esportivo médio"),
    MEDIUM_SEDAN("Sedã médio"),
    MEDIUM_FURGON("Furgão médio"),
    COMPACT_CONVERTIBLE("Conversível compacto"),
    LARGE_SPORTS("Esportivo grande"),
    COMPACT_PICKUP("Picape compacta"),
    TOURING("Touring"),
    STREET("Street"),
    SPORT("Sport"),
    CUSTOM("Custom"),
    QUAD_UTV("Quadriciclo UTV"),
    NAKED("Naked"),
    CUB("Cub"),
    CARGO_TRICYCLE("Triciclo de Carga"),
    ATV_QUAD("Quadriciclo (ATV)"),
    MOPED("Mobilete"),
    TRICYCLE("Triciclo"),
    TRAIL("Trail"),
    SCOOTER("Scooter");

    private final String portugueseTranslation;

    ModelCategory(String portugueseTranslation) {
        this.portugueseTranslation = portugueseTranslation;
    }

    public String getPortugueseTranslation() {
        return portugueseTranslation;
    }

    public static ModelCategory fromString(String value) {

        for (ModelCategory category : ModelCategory.values()) {
            if (category.getPortugueseTranslation().equals(value)) {
                return category;
            }
        }

        throw new RuntimeException("Value not found: " + value);
    }
}
