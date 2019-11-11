package com.ertai87.ecobeetest.application.tasks;

import com.ertai87.ecobeetest.application.ElasticSearch;
import com.ertai87.ecobeetest.application.entities.EInputUserData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.bulk.BulkRequest;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Log4j2
@Component
@AllArgsConstructor
public class DataLoader {
    private ElasticSearch elasticSearch;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public void loadData(Scanner inputFileScanner) {
        log.info("Uploading data");
        BulkRequest bulkRequest = new BulkRequest();

        String line = inputFileScanner.nextLine();
        while (!"".equals(line.trim())) {
            String[] data = line.split("\"");

            String name = data[1];
            String location = data[3];
            double rvalue = Double.parseDouble(data[4]);

            String[] locationData = location.split("/");
            String country = locationData[0];
            String province = locationData[1];
            String city = locationData[2];

            bulkRequest = elasticSearch.bulkIndex(
                    bulkRequest,
                    objectMapper.writeValueAsString(new EInputUserData(name, rvalue, country, province, city))
            );

            line = inputFileScanner.nextLine();
        }
        elasticSearch.executeBulk(bulkRequest);
        log.info("Done uploading data");
    }
}
