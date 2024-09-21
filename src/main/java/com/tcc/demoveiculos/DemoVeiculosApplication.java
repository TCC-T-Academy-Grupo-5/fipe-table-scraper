package com.tcc.demoveiculos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.demoveiculos.models.FipeMonthReference;
import com.tcc.demoveiculos.repositories.FipeMonthReferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class DemoVeiculosApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoVeiculosApplication.class, args);

    }

}
