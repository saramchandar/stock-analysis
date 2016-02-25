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
    def Double oi
    def Double ema5
    def Double ema20

    def static Price rowMapperClosure(def it) {
        new Price(
                name: it.name,
                priceDate: it.date.toLocalDate(),
                time: it.time.toLocalTime(),
                open: it.open as Double,
                high: it.high as Double,
                low: it.low as Double,
                close: it.close as Double,
                volume: it.volume as Long,
                oi: it.oi as Double,
                ema5: it.ema5 as Double,
                ema20: it.ema20 as Double
        )
    }
}