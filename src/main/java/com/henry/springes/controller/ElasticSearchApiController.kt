package com.henry.springes.controller

import com.henry.springes.dto.TestElasticSearchMessage
import com.henry.springes.service.ElasticSearchService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/es")
class ElasticSearchApiController(
    private val elasticSearchService: ElasticSearchService
) {
    companion object : KLogging()

    @PostMapping("/{index}")
    fun index(
        @PathVariable("index") index: String,
        @RequestBody message: TestElasticSearchMessage
    ): ResponseEntity<String> {
        val result = elasticSearchService.create(index, message)

        return ResponseEntity.ok().body(result.block().toString())
    }

    @GetMapping("/{index}/{id}")
    fun getTestMessage(
        @PathVariable("index") index: String,
        @PathVariable("id") id: String
    ): ResponseEntity<TestElasticSearchMessage> {
        val result = elasticSearchService.findById(index, id, TestElasticSearchMessage::class.java)

        return ResponseEntity.ok().body(result?.block() ?: throw RuntimeException())
    }

    @GetMapping("/{index}")
    fun getAllTestMessage(
        @PathVariable("index") index: String
    ): ResponseEntity<List<TestElasticSearchMessage>> {
        val resultList =
            elasticSearchService.findAll(listOf(index), TestElasticSearchMessage::class.java)
        return ResponseEntity.ok().body(resultList.collectList().block())
    }
}