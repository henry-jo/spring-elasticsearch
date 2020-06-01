package com.henry.springes.config

import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ElasticSearchProperties::class)
class ElasticSearchConfig(
    private val elasticSearchProperties: ElasticSearchProperties
) {

    @Bean(name = ["elasticSearchRestClient"])
    fun getRestClient(): RestHighLevelClient {
        return RestHighLevelClient(RestClient.builder(*elasticSearchProperties.httpHosts()))
    }
}