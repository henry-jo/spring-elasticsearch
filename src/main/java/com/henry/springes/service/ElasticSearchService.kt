package com.henry.springes.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.support.IndicesOptions
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.io.IOException

@Component
class ElasticSearchService(
    private val elasticSearchRestClient: RestHighLevelClient
) {
    companion object : KLogging()

    fun create(
        index: String,
        type: String,
        message: Any,
        id: String? = null
    ): Mono<String> {
        val request = try {
            IndexRequest(index, type, id).apply {
                source(
                    ObjectMapper().writeValueAsString(message),
                    XContentType.JSON
                )
            }
        } catch (e: JsonProcessingException) {
            logger.error("[Parsing Error] Message - $message", e)
            return Mono.empty()
        }

        return Mono.create { sink ->
            elasticSearchRestClient.indexAsync(
                request,
                RequestOptions.DEFAULT,
                object : ActionListener<IndexResponse> {
                    override fun onResponse(response: IndexResponse) {
                        if (response.status() in listOf(RestStatus.CREATED, RestStatus.OK)) {
                            sink.success(response.id)
                        } else {
                            sink.error(Exception("response status : ${response.status()}"))
                        }
                    }

                    override fun onFailure(e: Exception) {
                        logger.error("[ES_ERROR] " + e.message, e)
                    }
                })
        }
    }

    fun <T> findById(
        index: String,
        type: String,
        id: String?,
        clazz: Class<T>?
    ): Mono<T>? {
        val request = GetRequest(index, type, id)

        return Mono.create { sink: MonoSink<T> ->
            elasticSearchRestClient.getAsync(
                request,
                RequestOptions.DEFAULT,
                object : ActionListener<GetResponse> {
                    override fun onResponse(response: GetResponse) {
                        if (response.isExists) {
                            var obj: T? = null
                            try {
                                obj = ObjectMapper().readValue(response.sourceAsString, clazz)
                            } catch (e: IOException) {
                                logger.error("[Parsing Error]", e)
                            }
                            sink.success(obj)
                        } else {
                            sink.success(null)
                        }
                    }

                    override fun onFailure(e: java.lang.Exception) {
                        logger.error("[ES_ERROR] " + e.message, e)
                    }
                })
        }
    }

    fun <T> findAll(
        indexList: List<String>,
        type: String,
        clazz: Class<T>
    ): Flux<T> {
        val request = SearchRequest(*indexList.toTypedArray())
            .types(type)
            .source(SearchSourceBuilder().query(QueryBuilders.matchAllQuery()))
            .indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN)

        return Flux.create { sink ->
            elasticSearchRestClient.searchAsync(
                request,
                RequestOptions.DEFAULT,
                object : ActionListener<SearchResponse> {
                    override fun onResponse(response: SearchResponse) {
                        response.hits.forEach { item ->
                            try {
                                sink.next(
                                    ObjectMapper().readValue(item.sourceAsString, clazz)
                                )
                            } catch (e: Exception) {
                                logger.error("[Parsing Error]", e)
                            }
                        }
                        sink.complete()
                    }

                    override fun onFailure(e: Exception) {
                        logger.error("[ES_ERROR] " + e.message, e)
                        sink.error(e)
                    }
                })
        }
    }
}