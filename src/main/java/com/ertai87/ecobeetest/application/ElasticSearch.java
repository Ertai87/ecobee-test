package com.ertai87.ecobeetest.application;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Map;

@Log4j2
@Component
public class ElasticSearch {

    @Setter
    @Getter
    private RestHighLevelClient restClient;

    @Value("${elasticsearch.hostnames}")
    private String[] hostnames;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.index.name}")
    private String indexName;

    private final int MAX_BULK_ACTIONS = 10000;

    @SneakyThrows
    @PostConstruct
    public void postContruct() {
        HttpHost[] hosts = Arrays.stream(hostnames).map(h -> new HttpHost(h, port, "http")).toArray(HttpHost[]::new);
        this.restClient = new RestHighLevelClient(RestClient.builder(hosts));
    }

    @PreDestroy
    @SneakyThrows
    public void preDestroy() {
        restClient.close();
    }

    @SneakyThrows
    public void createIndex(String source){
        IndicesClient indicesClient = restClient.indices();
        boolean exists = indicesClient.exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
        if (exists) {
            indicesClient.delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
        }
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName)
                .source(source, XContentType.JSON);
        indicesClient.create(createIndexRequest, RequestOptions.DEFAULT);

        log.info("Created index {}", indexName);
    }

    @SneakyThrows
    public BulkRequest bulkIndex(BulkRequest bulkRequest, String source){
        bulkRequest.add(new IndexRequest(indexName).source(source, XContentType.JSON));
        if (bulkRequest.numberOfActions() >= MAX_BULK_ACTIONS){
            executeBulk(bulkRequest);
            return new BulkRequest();
        }
        return bulkRequest;
    }

    @SneakyThrows
    public void executeBulk(BulkRequest bulkRequest){
        restClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        log.info("Executed {} requests", bulkRequest.numberOfActions());
    }

    @SneakyThrows
    public long countItems(QueryBuilder query){
        SearchSourceBuilder source = new SearchSourceBuilder().query(query);
        CountRequest request = new CountRequest(indexName).source(source);
        return restClient.count(request, RequestOptions.DEFAULT).getCount();
    }

    @SneakyThrows
    public Map<String, Object> searchOne(SearchSourceBuilder source){
        SearchResponse response = restClient.search(new SearchRequest(indexName).source(source), RequestOptions.DEFAULT);
        return response.getHits().getAt(0).getSourceAsMap();
    }
}
