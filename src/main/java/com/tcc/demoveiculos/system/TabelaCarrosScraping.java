package com.tcc.demoveiculos.system;

import com.tcc.demoveiculos.models.*;
import com.tcc.demoveiculos.repositories.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TabelaCarrosScraping implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TabelaCarrosScraping.class);

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private YearRepository yearRepository;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private FipePriceRepository fipePriceRepository;

    @Value("${fipe-table.website.baseurl}")
    private String baseUrl;

    @Value("${fipe-table.website.useragent}")
    private String userAgent;

    private Map<String, Integer> monthsMap = new HashMap<>() {{
        put("Janeiro", 1);
        put("Fevereiro", 2);
        put("Março", 3);
        put("Abril", 4);
        put("Maio", 5);
        put("Junho", 6);
        put("Julho", 7);
        put("Agosto", 8);
        put("Setembro", 9);
        put("Outubro", 10);
        put("Novembro", 11);
        put("Dezembro", 12);
    }};

    @Override
    public void run(String... args) throws Exception {
        if (this.brandRepository.count() == 0) {
            this.loadBrandsByVehicleType("/marcas/carros", VehicleType.CAR);
            this.loadBrandsByVehicleType("/tabela-fipe-motos", VehicleType.MOTORCYCLE);
            this.loadBrandsByVehicleType("/marcas/caminhoes", VehicleType.TRUCK);
        }

        if (this.modelRepository.count() == 0) {
            this.loadModelsByVehicleType("/carros", VehicleType.CAR);
            this.loadModelsByVehicleType("/motos", VehicleType.MOTORCYCLE);
            this.loadModelsByVehicleType("/caminhoes", VehicleType.TRUCK);
        }

        if (this.yearRepository.count() == 0) {
            this.loadYearsByVehicleType("/anos_modelos/carros", VehicleType.CAR);
            this.loadYearsByVehicleType("/anos_modelos/motos", VehicleType.MOTORCYCLE);
            this.loadYearsByVehicleType("/anos_modelos/caminhoes", VehicleType.TRUCK);
        }

        if (this.versionRepository.count() == 0) {
            this.loadVersionsByVehicleType("/modelo/carros", VehicleType.CAR);
            this.loadVersionsByVehicleType("/modelo/motos", VehicleType.MOTORCYCLE);
            this.loadVersionsByVehicleType("/modelo/caminhoes", VehicleType.TRUCK);
        }

        if (this.fipePriceRepository.count() == 0) {
            this.loadPrices();
        }

        this.loadCategories();

//        JsonExporter.export("src/main/resources/datav3/brands/",
//                            "brands",
//                            this.brandRepository.findAll(),
//                            1000,
//                            Brand::mapToBrandDTO);
//
//        JsonExporter.export("src/main/resources/datav3/models/",
//                            "models",
//                            this.modelRepository.findAll(),
//                            1000,
//                            Model::mapToModelDTO);
//
//        JsonExporter.export("src/main/resources/datav3/years/",
//                            "years",
//                            this.yearRepository.findAll(),
//                            1400,
//                            Year::mapToYearDTO);
//
//        JsonExporter.export("src/main/resources/datav3/versions/",
//                            "versions",
//                            this.versionRepository.findAll(),
//                            700,
//                            Version::mapToVersionDTO);
//
//        JsonExporter.export("src/main/resources/datav3/fipeprices/",
//                            "fipeprices",
//                            this.fipePriceRepository.findAll(),
//                            1400,
//                            FipePrice::mapToFipePriceDTO);
    }

    private void loadBrandsByVehicleType(String path, VehicleType vehicleType) throws IOException {
        Document document = Jsoup.connect(this.baseUrl + path)
                .userAgent(this.userAgent)
                .get();
        Elements links = document.getElementsByClass("botao_fake1");
        List<Brand> brands = new ArrayList<>();

        links.forEach(link -> {
            String brandName = link.getElementsByTag("a").text();

            String brandUrl = link.getElementsByTag("a").attr("href");
            String brandUrlName = brandUrl.substring(brandUrl.lastIndexOf("/") + 1);

            Brand brandToAdd = new Brand();
            brandToAdd.setName(brandName);
            brandToAdd.setUrlPathName(brandUrlName);
            brandToAdd.setVehicleType(vehicleType);
            log.info("Adding brand {} to the database...", brandName);
            brands.add(brandToAdd);
        });

        this.brandRepository.saveAll(brands);
        log.info("A total of {} {} brands have been added to the database", brands.size(), vehicleType);
    }

    private void loadModelsByVehicleType(String path, VehicleType vehicleType) throws IOException {
        List<Brand> brands = this.brandRepository.findAllByVehicleType(vehicleType);

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
                        Model model = new Model();

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

    private void loadYearsByVehicleType(String path, VehicleType vehicleType) throws IOException {
        List<Model> models = this.modelRepository.findAllByBrand_VehicleType(vehicleType);

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
                        Year year = new Year();

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

    private void loadVersionsByVehicleType(String path, VehicleType vehicleType) throws IOException {
        List<Year> years = this.yearRepository.findByModel_Brand_VehicleType(vehicleType);

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

    private void loadPrices() {
        List<Version> versions = this.versionRepository.findAll();

        AtomicInteger totalPricesSaved = new AtomicInteger();
        String errorsFilePath = "src/main/resources/error_load_prices_version_ids" + Instant.now().toString().replace(":", "-") + ".txt";

        versions.forEach(version -> {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            String modelNameForLogging = version.getYear().getModel().getBrand().getName() + " " + version.getName();

            int maxRetries = 3;
            int attempt = 0;

            while (attempt < maxRetries) {
                try {
                    Document document = Jsoup.connect(version.getFullUrl())
                            .userAgent(this.userAgent)
                            .get();

                    Elements priceRows = new Elements(document.select(".tabela-historico tbody tr").stream().limit(3).toList());

                    priceRows.forEach(row -> {
                        String monthText = row.select(".valor_normal").text().trim();
                        String[] monthTextParts = monthText.split("/");
                        String priceText = row.select("td").get(1).text().trim();

                        Integer month = this.monthsMap.get(monthTextParts[0]);
                        Integer year = Integer.valueOf(monthTextParts[1]);
                        Double price = Double.valueOf(priceText.split(" ")[1].replace(".", ""));

                        FipePrice fipePrice = new FipePrice();
                        fipePrice.setMonth(month);
                        fipePrice.setYear(year);
                        fipePrice.setPrice(price);
                        fipePrice.setVersion(version);
                        log.info("Adding price for {}, month {}/{} to the database...", modelNameForLogging, month, year);
                        this.fipePriceRepository.save(fipePrice);
                        totalPricesSaved.getAndIncrement();
                    });

                    break;
                } catch (IOException e) {
                    attempt++;
                    log.error("Attempt {} failed to retrieve prices for {} {}: {}",
                              attempt, version.getYear().getModel().getBrand().getName(), version.getName(), e.getMessage());
                    if (attempt >= maxRetries) {
                        this.saveErrorIdsToFile(version.getId().toString(), errorsFilePath);
                        log.error("Failed to retrieve prices for {} {} after {} retries. ID saved to error log.",
                                  version.getYear().getModel().getBrand().getName(), version.getName(), maxRetries);
                    }


                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread was interrupted", ie);
                    }
                } catch (RuntimeException e) {
                    log.error("Failed to retrieve prices for {} {} due to runtime error: {}",
                              version.getYear().getModel().getBrand().getName(), version.getName(), e.getMessage());
                    this.saveErrorIdsToFile(version.getId().toString(), errorsFilePath);
                    break;
                }

            }
        });

        log.info("A total of {} prices have been added to the database", totalPricesSaved.get());
    }

    private void loadCategories() {
        List<String> modelIds = this.modelRepository.findAll().stream().map(Model::getId).toList();
        List<Version> firstVehiclesForEachModel = this.versionRepository.findRandomVersionsByModelIds(modelIds);

        firstVehiclesForEachModel.forEach(version -> {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            int maxRetries = 3;
            int attempt = 0;

            while (attempt < maxRetries) {
                try {
                    Document document = Jsoup.connect(version.getFullUrl())
                            .userAgent(this.userAgent)
                            .get();

                    Element categoryRow = document.select("tr:has(td:contains(Categoria))").first();

                    if (categoryRow != null) {
                        String categoryText = categoryRow.select("td").get(1).text();
                        System.out.println(version.getYear().getModel().getName() + ": " + categoryText);
                    }
                } catch (IOException e) {

                }
            }
        });
    }

    private void saveErrorIdsToFile(String id, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(id);
            writer.newLine();
        } catch (IOException e) {
            log.error("Failed to save error id {} to file {}", id, e.getMessage());
        }
    }
}
