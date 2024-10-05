package com.tcc.demoveiculos.system;

import com.tcc.demoveiculos.modelsv3.BrandV3;
import com.tcc.demoveiculos.modelsv3.ModelV3;
import com.tcc.demoveiculos.modelsv3.VehicleTypeV3;
import com.tcc.demoveiculos.modelsv3.YearV3;
import com.tcc.demoveiculos.repositoriesv3.BrandRepositoryV3;
import com.tcc.demoveiculos.repositoriesv3.ModelRepositoryV3;
import com.tcc.demoveiculos.repositoriesv3.YearRepositoryV3;
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

    @Autowired
    private ModelRepositoryV3 modelRepository;

    @Autowired
    private YearRepositoryV3 yearRepository;

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

        if (this.modelRepository.count() == 0) {
            this.loadModelsByVehicleType("/carros", VehicleTypeV3.CAR);
            this.loadModelsByVehicleType("/motos", VehicleTypeV3.MOTORCYCLE);
            this.loadModelsByVehicleType("/caminhoes", VehicleTypeV3.TRUCK);
        }

        if (this.yearRepository.count() == 0) {
            this.loadYearsByVehicleType("/anos_modelos/carros", VehicleTypeV3.CAR);
            this.loadYearsByVehicleType("/anos_modelos/motos", VehicleTypeV3.MOTORCYCLE);
            this.loadYearsByVehicleType("/anos_modelos/caminhoes", VehicleTypeV3.TRUCK);
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

    private void loadModelsByVehicleType(String path, VehicleTypeV3 vehicleType) throws IOException {
        List<BrandV3> brands = this.brandRepository.findAllByVehicleType(vehicleType);
        List<ModelV3> models = new ArrayList<>();

        brands.forEach(brand -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            int maxRetries = 3;
            int attempt = 0;

            while (attempt < maxRetries) {
                try {
                    Document document = Jsoup.connect(this.baseUrl + path + "/" + brand.getUrlPathName())
                            .userAgent(this.userAgent)
                            .get();

                    String brandImageUrl = document.select(".pagina_expandida h1 img").attr("src");
                    brand.setImageUrl(brandImageUrl);

                    Elements modelLinks = document.select(".box_todos_modelos a");
                    modelLinks.forEach(modelLink -> {
                        ModelV3 model = new ModelV3();

                        String modelName = modelLink.getElementsByClass("modelo_base2").text();
                        String modelImageUrl = modelLink.getElementsByClass("img_modelo").attr("src");

                        String modelUrl = modelLink.getElementsByTag("a").attr("href");
                        String modelUrlName = modelUrl.substring(modelUrl.lastIndexOf("/") + 1);

                        model.setName(modelName);
                        model.setImageUrl(modelImageUrl);
                        model.setUrlPathName(modelUrlName);
                        model.setBrand(brand);
                        log.info("Adding model {} to the database...", modelName);
                        models.add(model);
                    });

                    log.info("Updating brand {} image url...", brand.getName());
                    log.info("Finished preloading models for brand {}", brand.getName());
                    this.brandRepository.save(brand);

                    break;
                } catch (IOException e) {
                    attempt++;
                    log.error("Attempt {} failed to retrieve models for brand {}: {}", attempt, brand.getName(), e.getMessage());
                    if (attempt >= maxRetries) {
                        throw new RuntimeException("Failed to retrieve models for brand " + brand.getName() + " after " + maxRetries + " retries");
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread was interrupted", ie);
                    }
                }
            }

        });

        this.modelRepository.saveAll(models);
        log.info("A total of {} {} models have been added to the database", models.size(), vehicleType);
    }

    private void loadYearsByVehicleType(String path, VehicleTypeV3 vehicleType) throws IOException {
        List<ModelV3> models = this.modelRepository.findAllByBrand_VehicleType(vehicleType);
        List<YearV3> years = new ArrayList<>();

        models.forEach(model -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            int maxRetries = 3;
            int attempt = 0;

            while (attempt < maxRetries) {
                String url = this.baseUrl + path + "/" + model.getBrand().getUrlPathName() + "/" + model.getUrlPathName();

                try {
                    Document document = Jsoup.connect(url)
                            .userAgent(this.userAgent)
                            .get();

                    Elements links = document.getElementsByClass("link");

                    links.forEach(link -> {
                        YearV3 year = new YearV3();

                        String yearName = link.select(".link_ano").text();

                        String yearUrl = link.select(".botao_fake3").attr("href");
                        String yearUrlName = yearUrl.substring(yearUrl.lastIndexOf("/") + 1);

                        year.setName(yearName);
                        year.setUrlPathName(yearUrlName);
                        year.setModel(model);
                        log.info("Adding year {} for {} {} to the database...", yearName, model.getBrand().getName(), model.getName());
                        years.add(year);
                    });


                    log.info("Finished preloading years for {} {}", model.getBrand().getName(), model.getName());
                    break;
                } catch (IOException e) {
                    attempt++;
                    log.error("Attempt {} failed to retrieve years for model {}: {}", attempt, model.getName(), e.getMessage());
                    if (attempt >= maxRetries) {
                        throw new RuntimeException("Failed to retrieve years for model " + model.getName() + " after " + maxRetries + " retries");
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread was interrupted", ie);
                    }
                }
            }
        });

        this.yearRepository.saveAll(years);
        log.info("A total of {} years have been added to the database", years.size());
    }
}
