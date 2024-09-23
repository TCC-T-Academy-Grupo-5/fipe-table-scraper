package com.tcc.demoveiculos.system;

import com.tcc.demoveiculos.models.*;
import com.tcc.demoveiculos.repositories.BrandRepository;
import com.tcc.demoveiculos.repositories.ModelRepository;
import com.tcc.demoveiculos.services.VehicleYearScrapingService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScrapingTest implements CommandLineRunner {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ScrapingTest.class);
    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private VehicleYearScrapingService vehicleYearScrapingService;

    @Value("${fipe-table.website.baseurl}")
    private String baseUrl;

    @Value("${fipe-table.website.useragent}")
    private String userAgent;


    @Override
    public void run(String... args) throws Exception {
        if (this.brandRepository.count() == 0) {
            this.loadBrandsByVehicleType("/carros", VehicleType.CAR);
            this.loadBrandsByVehicleType("/motos", VehicleType.MOTORCYCLE);
            this.loadBrandsByVehicleType("/caminhoes", VehicleType.TRUCK);
        }

        if (this.modelRepository.count() == 0) {
            this.loadModelsByVehicleType("/carros", VehicleType.CAR);
            this.loadModelsByVehicleType("/motos", VehicleType.MOTORCYCLE);
            this.loadModelsByVehicleType("/caminhoes", VehicleType.TRUCK);
        }

        this.loadYearsByVehicleType("/carros", VehicleType.CAR);
        this.loadYearsByVehicleType("/motos", VehicleType.MOTORCYCLE);
        this.loadYearsByVehicleType("/caminhoes", VehicleType.TRUCK);
    }

    private void loadBrandsByVehicleType(String uri, VehicleType vehicleType) throws IOException {
        Document document = Jsoup.connect(this.baseUrl + uri)
                .userAgent(userAgent)
                .get();
        Elements links = document.getElementsByClass("fipe_link");
        List<Brand> brands = new ArrayList<>();

        links.forEach(link -> {
            String brandName = link.getElementsByTag("a").text();

            String brandUrl = link.getElementsByTag("a").attr("href");
            String brandUrlName = brandUrl.substring(brandUrl.lastIndexOf("/") + 1);

            Brand brandToAdd = new Brand();
            brandToAdd.setName(brandName);
            brandToAdd.setUrlPathName(brandUrlName);
            brandToAdd.setVehicleType(vehicleType);
            log.info("Adding brand {} to the database", brandName);
            brands.add(brandToAdd);
        });

        this.brandRepository.saveAll(brands);
        log.info("All {} brands added to the database", vehicleType.getDescription());
    }

    private void loadModelsByVehicleType(String uri, VehicleType vehicleType) throws IOException {
        List<Brand> brands = this.brandRepository.findAllByVehicleType(vehicleType);
        List<Model> models = new ArrayList<>();

        brands.forEach(brand -> {
            try {
                Document document = Jsoup.connect(this.baseUrl + uri + "/" + brand.getUrlPathName())
                        .userAgent(this.userAgent)
                        .get();

                Elements rows = document.select("table.fipeTableModels tbody tr");
                List<Element> filteredRows = rows.stream()
                        .filter(row -> row.select("td.fipeTableHead").isEmpty())
                        .toList();

                for (Element row : filteredRows) {
                    String modelName = row.select("td a").get(1).text();

                    String modelUrl = row.select("td a").get(1).attr("href");
                    String modelUrlName = modelUrl.substring(modelUrl.lastIndexOf("/") + 1);

                    Model model = new Model();
                    model.setName(modelName);
                    model.setUrlPathName(modelUrlName);
                    model.setBrand(brand);

                    log.info("Adding model {} to the database", modelName);
                    models.add(model);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (IndexOutOfBoundsException e) {
                log.error("Error in brand {}: {}", brand.getName(), e.getMessage());
                throw new RuntimeException(e);
            }

            this.modelRepository.saveAll(models);
            log.info("All {} of the brand {} added to the database", vehicleType.getDescription(), brand.getName());
        });
    }

    private void loadYearsByVehicleType(String uri, VehicleType vehicleType) throws IOException {
        List<Model> models = this.modelRepository.findAllByBrand_VehicleType(vehicleType);

        models.forEach(model -> {
            if (!model.getYears().isEmpty()) {
                return;
            }
            this.vehicleYearScrapingService.processModelYears(model, this.baseUrl, this.userAgent, uri);
        });
    }
}
