package com.tcc.demoveiculos.system;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.demoveiculos.models.Brand;
import com.tcc.demoveiculos.models.FipeMonthReference;
import com.tcc.demoveiculos.repositories.BrandRepository;
import com.tcc.demoveiculos.repositories.FipeMonthReferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
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
    private ObjectMapper objectMapper;

    private Logger logger = Logger.getLogger(ApiTest.class.getName());

    private WebClient webClient = WebClient.builder().baseUrl("https://fipe.parallelum.com.br/api/v2").build();

    @Override
    public void run(String... args) throws Exception {
        if (this.fipeMonthReferenceRepository.count() == 0) {
            this.populateReferences();
        }

        if (this.brandRepository.count() == 0) {
            this.populateBrands();
        }

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
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(result -> {
                    try {
                        List<FipeMonthReference> references = this.objectMapper.readValue(result, new TypeReference<>() {});

                        references.stream().peek(ref -> {
                            String month = ref.getMonth();
                            String[] monthAndYear = month.split("/");
                            String monthStr = monthAndYear[0];
                            String monthNumber = months.get(monthStr);
                            ref.setMonth(monthNumber + "/" + monthAndYear[1]);
                        }).forEach(ref -> {
                            this.logger.log(Level.INFO, "Salvando: " + ref.getMonth());
                            this.fipeMonthReferenceRepository.save(ref);
                        });
                    } catch (JsonProcessingException e) {
                        this.logger.log(Level.SEVERE, "Erro ao converter JSON: " + e.getMessage());
                    }
                });
    }

    private void populateBrands() {
        this.populateCarBrands();
    }

    private void populateCarBrands() {
        webClient.get()
                .uri("/cars/brands")
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(result -> {
                    try {
                        List<Brand> carBrands = this.objectMapper.readValue(result, new TypeReference<>() {});

                        carBrands.forEach(brand -> {
                            this.logger.log(Level.INFO, "Salvando: " + brand.getName());
                            this.brandRepository.save(brand);
                        });
                    } catch (JsonProcessingException e) {
                        this.logger.log(Level.SEVERE, "Erro ao converter JSON: " + e.getMessage());
                    }
                });

    }

    private void populateMotorcycleBrands() {

    }

    private void populateTruckBrands() {

    }
}
