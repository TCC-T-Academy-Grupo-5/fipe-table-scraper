package com.tcc.demoveiculos.system;

import com.tcc.demoveiculos.modelsv3.*;
import com.tcc.demoveiculos.repositoriesv3.BrandRepositoryV3;
import com.tcc.demoveiculos.repositoriesv3.ModelRepositoryV3;
import com.tcc.demoveiculos.repositoriesv3.VersionRepository;
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

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TabelaCarrosScraping implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TabelaCarrosScraping.class);

    @Autowired
    private BrandRepositoryV3 brandRepository;

    @Autowired
    private ModelRepositoryV3 modelRepository;

    @Autowired
    private YearRepositoryV3 yearRepository;

    @Autowired
    private VersionRepository versionRepository;

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

        if (this.versionRepository.count() == 0) {
            this.loadVersionsByVehicleType("/modelo/carros", VehicleTypeV3.CAR, List.of(), false);
            this.loadVersionsByVehicleType("/modelo/motos", VehicleTypeV3.MOTORCYCLE, List.of(), false);
            this.loadVersionsByVehicleType("/modelo/caminhoes", VehicleTypeV3.TRUCK, List.of(), false);
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

        AtomicInteger totalModelsSaved = new AtomicInteger();
        String errorsFilePath = "src/main/resources/error_load_models_brand_ids" + Instant.now().toString().replace(":", "-") + ".txt";

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

                        String modelName = modelLink.getElementsByClass("modelo_base2").text().trim();
                        String modelImageUrl = modelLink.getElementsByClass("img_modelo").attr("src").trim();

                        String modelUrl = modelLink.getElementsByTag("a").attr("href").trim();
                        String modelUrlName = modelUrl.substring(modelUrl.lastIndexOf("/") + 1).trim();

                        model.setName(modelName);
                        model.setImageUrl(modelImageUrl);
                        model.setUrlPathName(modelUrlName);
                        model.setBrand(brand);
                        log.info("Adding model {} to the database...", modelName);
                        this.modelRepository.save(model);
                    });

                    log.info("Updating brand {} image url...", brand.getName());
                    log.info("Finished saving models for brand {}", brand.getName());
                    this.brandRepository.save(brand);

                    break;
                } catch (IOException e) {
                    attempt++;
                    log.error("Attempt {} failed to retrieve models for brand {}: {}", attempt, brand.getName(), e.getMessage());
                    if (attempt >= maxRetries) {
                        this.saveErrorIdsToFile(brand.getId().toString(), errorsFilePath);
                        log.error("Failed to retrieve models for brand {} after {} retries. ID saved to error log.", brand.getName(), maxRetries);
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

        log.info("A total of {} {} models have been added to the database", totalModelsSaved, vehicleType);
    }

    private void loadYearsByVehicleType(String path, VehicleTypeV3 vehicleType) throws IOException {
        List<ModelV3> models = this.modelRepository.findAllByBrand_VehicleType(vehicleType);

        AtomicInteger totalYearsSaved = new AtomicInteger();
        String errorsFilePath = "src/main/resources/error_load_years_model_ids" + Instant.now().toString().replace(":", "-") + ".txt";

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

                        String yearName = link.select(".link_ano").text().trim();

                        String yearUrl = link.select(".botao_fake3").attr("href").trim();
                        String yearUrlName = yearUrl.substring(yearUrl.lastIndexOf("/") + 1).trim();

                        year.setName(yearName);
                        year.setUrlPathName(yearUrlName);
                        year.setModel(model);
                        log.info("Adding year {} for {} {} to the database...", yearName, model.getBrand().getName(), model.getName());
                        this.yearRepository.save(year);
                    });


                    log.info("Finished saving years for {} {}", model.getBrand().getName(), model.getName());
                    break;
                } catch (IOException e) {
                    attempt++;
                    log.error("Attempt {} failed to retrieve years for model {}: {}", attempt, model.getName(), e.getMessage());
                    if (attempt >= maxRetries) {
                        this.saveErrorIdsToFile(model.getId().toString(), errorsFilePath);
                        log.error("Failed to retrieve years for model {} after {} retries. ID saved to error log.", model.getName(), maxRetries);
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

        log.info("A total of {} years have been added to the database", totalYearsSaved);
    }

    private void loadVersionsByVehicleType(String path, VehicleTypeV3 vehicleType) throws IOException {
        List<YearV3> years = this.yearRepository.findByModel_Brand_VehicleType(vehicleType);

        AtomicInteger totalVersionsSaved = new AtomicInteger();
        String errorsFilePath = "src/main/resources/error_load_versions_year_ids" + Instant.now().toString().replace(":", "-") + ".txt";

        years.forEach(year -> {
            String brandName = year.getModel().getBrand().getName();
            String modelName = year.getModel().getName();
            String yearName = year.getName();

            String url = this.baseUrl + path + "/" +
                    year.getModel().getBrand().getUrlPathName() + "/" +
                    year.getModel().getUrlPathName() + "/" +
                    year.getUrlPathName();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            int maxRetries = 3;
            int attempt = 0;

            while (attempt < maxRetries) {
                try {
                    Document document = Jsoup.connect(url)
                            .userAgent(this.userAgent)
                            .get();

                    Elements links = document.getElementsByClass("link");

                    links.forEach(link -> {
                        Version version = new Version();

                        String fullUrl = link.select("tr").attr("data-url").trim();
                        String[] urlParts = fullUrl.split("/");
                        String versionUrlPathName = urlParts[urlParts.length - 2].trim();

                        String fipeCode = link.select(".codigo_fipe").text().trim();
                        String versionName = link.select(".link_ano").text().trim();

                        version.setName(versionName);
                        version.setFipeCode(fipeCode);
                        version.setUrlPathName(versionUrlPathName);
                        version.setFullUrl(fullUrl);
                        version.setYear(year);
                        log.info("Adding version {} for {} {} {} to the database...", versionName, brandName, modelName, yearName);
                        totalVersionsSaved.getAndIncrement();
                        this.versionRepository.save(version);
                    });

                    break;
                } catch (IOException e) {
                    attempt++;
                    log.error("Attempt {} failed to retrieve versions for {} {} {}: {}", attempt, brandName, modelName, yearName, e.getMessage());
                    if (attempt >= maxRetries) {
                        this.saveErrorIdsToFile(year.getId().toString(), errorsFilePath);
                        log.error("Failed to retrieve versions for {} {} {} after {} retries. ID saved to error log.", brandName, modelName, yearName, maxRetries);
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

        log.info("A total of {} versions have been added to the database", totalVersionsSaved.get());
    }

    private void saveErrorIdsToFile(String id, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(id);
            writer.newLine();
        } catch (IOException e) {
            log.error("Failed to save error id {} to file {}", id, e.getMessage());
        }
    }

    private void retryFailedVersionScraping(String path) throws IOException {
        List<UUID> ids = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ids.add(UUID.fromString(line));
            }

        } catch (IOException e) {
            log.error("Failed to read file {}", path);
        }

        List<YearV3> years = this.yearRepository.findAllById(ids);

        List<YearV3> carYears = years.stream()
                .filter(year -> year.getModel().getBrand().getVehicleType() == VehicleTypeV3.CAR)
                .toList();

        List<YearV3> motorcycleYears = years.stream()
                .filter(year -> year.getModel().getBrand().getVehicleType() == VehicleTypeV3.MOTORCYCLE)
                .toList();

        List<YearV3> truckYears = years.stream()
                .filter(year -> year.getModel().getBrand().getVehicleType() == VehicleTypeV3.TRUCK)
                .toList();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, false))) {

        } catch (IOException e) {
            log.error("Failed to clear the file {}", path);
        }

        this.loadVersionsByVehicleType("/modelo/carros", VehicleTypeV3.CAR, carYears, true);
        this.loadVersionsByVehicleType("/modelo/motos", VehicleTypeV3.MOTORCYCLE, motorcycleYears, true);
        this.loadVersionsByVehicleType("/modelo/caminhoes", VehicleTypeV3.TRUCK, truckYears, true);
    }
}
