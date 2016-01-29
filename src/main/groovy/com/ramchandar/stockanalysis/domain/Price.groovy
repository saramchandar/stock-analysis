package com.ramchandar.stockanalysis.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalDate
import java.time.LocalTime

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = ["name", "date"])
class Price {
    def String name
    def LocalDate priceDate
    def LocalTime time
    def Double open
    def Double high
    def Double low
    def Double close
    def Long volume
}