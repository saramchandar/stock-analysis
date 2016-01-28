package com.ramchandar.stockanalysis

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalDate

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = ["name", "date"])
class Price {
    def String name
    def LocalDate priceDate
    def Double open
    def Double high
    def Double low
    def Double close
    def Double volume
}