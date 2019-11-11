package com.ertai87.ecobeetest.application.tasks;

import com.ertai87.ecobeetest.application.ElasticSearch;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;

@Component
@AllArgsConstructor
public class SetupElasticSearch {
    private ElasticSearch elasticSearch;

    @SneakyThrows
    public void SetupElasticSearch(){
        Resource mappingFile = new ClassPathResource("/elasticsearch-mapping.json");
        FileInputStream fileInputStream = new FileInputStream(mappingFile.getFile());
        byte[] fileBytes = IOUtils.toByteArray(fileInputStream);
        elasticSearch.createIndex(new String(fileBytes));
    }
}
