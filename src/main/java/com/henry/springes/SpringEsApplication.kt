package com.henry.springes

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class SpringEsApplication

fun main(args: Array<String>) {
    SpringApplication.run(SpringEsApplication::class.java, *args)
}