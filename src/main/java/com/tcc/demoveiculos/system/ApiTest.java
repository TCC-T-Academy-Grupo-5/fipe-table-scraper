package com.tcc.demoveiculos.system;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.demoveiculos.models.Brand;
import com.tcc.demoveiculos.models.FipeMonthReference;
import com.tcc.demoveiculos.models.Model;
import com.tcc.demoveiculos.models.VehicleType;
import com.tcc.demoveiculos.repositories.BrandRepository;
import com.tcc.demoveiculos.repositories.FipeMonthReferenceRepository;
import com.tcc.demoveiculos.repositories.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ApiTest implements CommandLineRunner {

    @Autowired
    private FipeMonthReferenceRepository fipeMonthReferenceRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Logger logger = Logger.getLogger(ApiTest.class.getName());

    private WebClient webClient = WebClient.builder().baseUrl("https://fipe.parallelum.com.br/api/v2").build();

    @Value("${fipe-api.subscription.token}")
    private String subscriptionToken;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (this.fipeMonthReferenceRepository.count() == 0) {
            this.populateReferences();
        }

        this.populateBrands();

        this.populateModels();
    }

    private void populateReferences() {
        Map<String, String> months = new HashMap<>();
        months.put("janeiro", "01");
        months.put("fevereiro", "02");
        months.put("março", "03");
        months.put("abril", "04");
        months.put("maio", "05");
        months.put("junho", "06");
        months.put("julho", "07");
        months.put("agosto", "08");
        months.put("setembro", "09");
        months.put("outubro", "10");
        months.put("novembro", "11");
        months.put("dezembro", "12");

        webClient.get()
                .uri("/references")
                .header("X-Subscription-Token", this.subscriptionToken)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(result -> {
                    try {
                        List<FipeMonthReference> references = this.objectMapper.readValue(result, new TypeReference<>() {
                        });

                        references.forEach(ref -> {
                            this.logger.log(Level.INFO, "Preparando: " + ref.getMonth());
                            String month = ref.getMonth();
                            String[] monthAndYear = month.split("/");
                            String monthStr = monthAndYear[0];
                            String monthNumber = months.get(monthStr);
                            ref.setMonth(monthNumber + "/" + monthAndYear[1]);
                        });

                        return Mono.just(this.fipeMonthReferenceRepository.saveAll(references));
                    } catch (JsonProcessingException e) {
                        this.logger.log(Level.SEVERE, "Erro ao converter JSON: " + e.getMessage());
                        return Mono.error(e);
                    }
                })
                .subscribe(
                        success -> this.logger.log(Level.INFO, "Todas as referências foram salvas no banco de dados"),
                        error -> this.logger.log(Level.SEVERE, "Erro ao salvar referências: " + error.getMessage())
                );
    }

    private void populateBrands() {
        if (this.brandRepository.count() == 0) {
            this.populateBrandsByType("/cars/brands", VehicleType.CAR);
            this.populateBrandsByType("/motorcycles/brands", VehicleType.MOTORCYCLE);
            this.populateBrandsByType("/trucks/brands", VehicleType.TRUCK);
        }
    }

    private void populateBrandsByType(String uri, VehicleType vehicleType) {
        webClient.get()
                .uri(uri)
                .header("X-Subscription-Token", this.subscriptionToken)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(result -> {
                    try {
                        List<Brand> brands = this.objectMapper.readValue(result, new TypeReference<>() {
                        });

                        brands.forEach(brand -> {
                            this.logger.log(Level.INFO, "Preparando: " + brand.getName());
                            brand.setVehicleType(vehicleType);
                        });

                        return Mono.just(this.brandRepository.saveAll(brands));
                    } catch (JsonProcessingException e) {
                        this.logger.log(Level.SEVERE, "Erro ao converter JSON: " + e.getMessage());
                        return Mono.error(e);
                    }
                })
                .subscribe(
                        success -> this.logger.log(Level.INFO, "Todas as marcas de " + vehicleType.getDescription() + " foram salvas no banco de dados"),
                        error -> this.logger.log(Level.SEVERE, "Erro ao salvar marcas de " + vehicleType.getDescription() + ": " + error.getMessage())
                );
    }

    private void populateModels() {
        if (this.modelRepository.count() == 0) {
            this.populateModelsByType("cars", VehicleType.CAR);
            this.populateModelsByType("motorcycles", VehicleType.MOTORCYCLE);
            this.populateModelsByType("trucks", VehicleType.TRUCK);
        }
    }

    private void populateModelsByType(String typeStr, VehicleType vehicleType) {
        List<Brand> brands = this.brandRepository.findAllByVehicleType(vehicleType);

        brands.forEach(brand -> {
            this.webClient.get()
                    .uri("/" + typeStr + "/brands/" + brand.getCode() + "/models")
                    .header("X-Subscription-Token", this.subscriptionToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(result -> {
                        try {
                            List<Model> models = this.objectMapper.readValue(result, new TypeReference<>() {});

                            models.forEach(model -> {
                                this.logger.log(Level.INFO, "Preparando: " + model.getName() + " , marca " + brand.getName());
                                model.setBrand(brand);
                            });

                            return Mono.just(this.modelRepository.saveAll(models));
                        } catch (JsonProcessingException e) {
                            this.logger.log(Level.SEVERE, "Erro ao converter JSON: " + e.getMessage());
                            return Mono.error(e);
                        }
                    })
                    .subscribe(
                            success -> this.logger.log(Level.INFO, "Todas os modelos de " + vehicleType.getDescription() + " da marca " + brand.getName() + " foram salvos no banco de dados"),
                            error -> {
                                this.logger.log(Level.SEVERE, "Erro ao salvar modelos de " + vehicleType.getDescription() + ": " + error.getMessage());
                                throw new RuntimeException(error);
                            }
                    );
        });
    }
}
