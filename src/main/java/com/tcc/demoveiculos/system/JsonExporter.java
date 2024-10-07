package com.tcc.demoveiculos.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class JsonExporter {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(JsonExporter.class);

    public static <T> void export(String path,
            String fileNamePrefix,
            List<T> collection,
            int maxItemsPerJsonFile,
            Function<T, Object> mapperFunction) {

        List<List<T>> partitionedList = Lists.partition(collection, maxItemsPerJsonFile);

        IntStream.range(0, partitionedList.size())
                .forEach(i -> {
                    List<T> partition = partitionedList.get(i);

                    List<Object> dtoList = partition.stream()
                            .map(mapperFunction)
                            .toList();

                    String fullFilePath = path + fileNamePrefix + "-" + (i + 1) + ".json";

                    try {

                        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fullFilePath), dtoList);

                    } catch (IOException e) {
                        log.error("Error writing json file: {}", e.getMessage());
                    }

                });
    }
}
