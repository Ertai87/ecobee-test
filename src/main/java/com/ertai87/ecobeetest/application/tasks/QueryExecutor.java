package com.ertai87.ecobeetest.application.tasks;

import com.ertai87.ecobeetest.application.ElasticSearch;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Component
@AllArgsConstructor
public class QueryExecutor {
    private ElasticSearch elasticSearch;

    @SneakyThrows
    public int executeRankingQuery(String name, String region) {
        SearchSourceBuilder getRvalueRequestSource = new SearchSourceBuilder()
                .fetchSource("rvalue", null)
                .size(1)
                .query(termQuery("name", name));
        double rvalue = (double)elasticSearch.searchOne(getRvalueRequestSource).get("rvalue");

        String[] locationData = region.split("/");
        BoolQueryBuilder locationQuery = boolQuery().must(termQuery("country", locationData[0]));
        if (locationData.length > 1){
            locationQuery = locationQuery.must(termQuery("province", locationData[1]));
        }
        if (locationData.length > 2){
            locationQuery = locationQuery.must(termQuery("city", locationData[2]));
        }
        double countAll = elasticSearch.countItems(locationQuery);

        QueryBuilder betterScoresQuery = locationQuery.must(rangeQuery("rvalue").gt(rvalue));
        double countGreater = elasticSearch.countItems(betterScoresQuery);

        double percentageGreater = countGreater / countAll * 100;
        return (int)Math.ceil((100.0 - percentageGreater) / 10);
    }
}
