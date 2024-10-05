package com.tcc.demoveiculos.system;

import com.tcc.demoveiculos.modelsv3.BrandV3;
import com.tcc.demoveiculos.modelsv3.VehicleTypeV3;
import com.tcc.demoveiculos.repositoriesv3.BrandRepositoryV3;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TabelaCarrosScraping implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TabelaCarrosScraping.class);
    @Autowired
    private BrandRepositoryV3 brandRepository;

    @Value("${fipe-table.website.baseurl}")
    private String baseUrl;

    @Value("${fipe-table.website.useragent}")
    private String userAgent;

    @Override
    public void run(String... args) throws Exception {
        if (this.brandRepository.count() == 0) {
            this.loadBrandsByVehicleType("/marcas/carros", VehicleTypeV3.CAR);
            this.loadBrandsByVehicleType("/tabela-fipe-motos", VehicleTypeV3.MOTORCYCLE);
            this.loadBrandsByVehicleType("/marcas/caminhoes", VehicleTypeV3.TRUCK);
        }
    }

    private void loadBrandsByVehicleType(String path, VehicleTypeV3 vehicleType) throws IOException {
        Document document = Jsoup.connect(this.baseUrl + path)
                .userAgent(this.userAgent)
                .get();
        Elements links = document.getElementsByClass("botao_fake1");
        List<BrandV3> brands = new ArrayList<>();

        links.forEach(link -> {
            String brandName = link.getElementsByTag("a").text();

            String brandUrl = link.getElementsByTag("a").attr("href");
            String brandUrlName = brandUrl.substring(brandUrl.lastIndexOf("/") + 1);

            BrandV3 brandToAdd = new BrandV3();
            brandToAdd.setName(brandName);
            brandToAdd.setUrlPathName(brandUrlName);
            brandToAdd.setVehicleType(vehicleType);
            log.info("Adding brand {} to the database...", brandName);
            brands.add(brandToAdd);
        });

        this.brandRepository.saveAll(brands);
        log.info("A total of {} {} brands have been added to the database", brands.size(), vehicleType);
    }
}
