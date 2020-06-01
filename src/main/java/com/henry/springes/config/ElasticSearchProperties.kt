package com.henry.springes.config

import org.apache.http.HttpHost
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.elasticsearch")
class ElasticSearchProperties {
    lateinit var hosts: List<String>

    fun httpHosts(): Array<HttpHost> = hosts.map { HttpHost.create(it) }.toTypedArray()
}