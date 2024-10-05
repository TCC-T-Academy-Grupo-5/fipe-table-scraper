package com.tcc.demoveiculos.services;

import com.tcc.demoveiculos.models.*;
import com.tcc.demoveiculos.repositories.FipeMonthReferenceRepository;
import com.tcc.demoveiculos.repositories.FipePriceRepository;
import com.tcc.demoveiculos.repositories.VehicleRepository;
import com.tcc.demoveiculos.repositories.YearRepository;
import jakarta.transaction.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleYearScrapingService {
    private static final Logger log = LoggerFactory.getLogger(VehicleYearScrapingService.class);

    @Autowired
    private YearRepository yearRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private FipePriceRepository fipePriceRepository;

    @Autowired
    private FipeMonthReferenceRepository fipeMonthReferenceRepository;

    @Transactional
    public void processModelYears(Model model, String baseUrl, String userAgent, String uri) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<Year> yearsToSave = new ArrayList<>();
        List<Vehicle> vehiclesToSave = new ArrayList<>();
        List<FipePrice> pricesToSave = new ArrayList<>();

        String completeUrl = baseUrl + uri + "/" + model.getBrand().getUrlPathName() + "/" + model.getUrlPathName();

        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                Document document = Jsoup.connect(completeUrl)
                        .userAgent(userAgent)
                        .get();

                Elements yearRows = document.select("tbody tr:not(.fipeTableHead)");
                yearRows.forEach(row -> {
                    String yearName = row.select("td:first-child a").text();
                    String yearUrl = row.select("td:first-child a").attr("href");
                    String yearUrlPathName = yearUrl.substring(yearUrl.lastIndexOf("/") + 1);

                    Year year = new Year();
                    year.setName(yearName);
                    year.setUrlPathName(yearUrlPathName);
                    year.setModel(model);
                    yearsToSave.add(year);

                    String fipeCode = document.select("div.DIVdetail > p:nth-child(2) > b > a").text();

                    Vehicle vehicle = new Vehicle();

                    vehicle.setYear(year);
                    vehicle.setFipeCode(fipeCode);

                    vehicle.setFipePrices(new ArrayList<>());

                    vehiclesToSave.add(vehicle);
                });

                Elements priceRows = document.select("tbody tr:not(.fipeTableHead)");

                priceRows.forEach(row -> {
                    String price = row.select("td:nth-child(2)").text();
                    FipePrice fipePrice = new FipePrice();

                    String monthText = document.select("div.DIVdetail > p:nth-child(6)").text();
                    String monthReference = monthText.split(": ")[1];

                    FipeMonthReference monthReferenceToSave = new FipeMonthReference();
                    monthReferenceToSave.setMonth(monthReference);

                    fipePrice.setFipeMonthReference(monthReferenceToSave);
                    fipePrice.setPrice(price);

                    pricesToSave.add(fipePrice);
                    fipeMonthReferenceRepository.save(monthReferenceToSave);
                });

                log.info("Saving {} years", model.getName());
                log.info("Saving {} vehicles", model.getName());

                List<FipePrice> finalPricesToSave = setVehiclesIntoPrices(vehiclesToSave, pricesToSave);

                yearRepository.saveAll(yearsToSave);
                vehicleRepository.saveAll(vehiclesToSave);
                fipePriceRepository.saveAll(finalPricesToSave);

                break;
            } catch (IOException e) {
                attempt++;
                log.error("Attempt {} failed to retrieve data for model {}: {}", attempt, model.getName(), e.getMessage());
                if (attempt >= maxRetries) {
                    throw new RuntimeException("Failed to retrieve data after " + maxRetries + " attempts for model: " + model.getName(), e);
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread was interrupted", ie);
                }
            }

        }
    }

    private List<FipePrice> setVehiclesIntoPrices(List<Vehicle> vehicles, List<FipePrice> fipePrices) {
        for (int i = 0; i < fipePrices.size(); i++) {
            FipePrice p = fipePrices.get(i);
            p.setVehicle(vehicles.get(i));
        }
        return fipePrices;
    }
}
