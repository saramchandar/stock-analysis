package com.ramchandar.stockanalysis

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DataProcessor {

    def DAO dao

    DataProcessor() {
        dao = new DAO()
    }

    def void processFile(String fileName) {
        def prices = []
        new FileReader(fileName).eachLine { String line, lineNo ->
            if (lineNo != 1) {
                def values = line.split(Constants.FIELD_SEPARATOR)

                try {
                    def price = new Price(
                            name: values[Constants.STOCK],
                            priceDate: LocalDate.parse(values[Constants.PRICE_DATE], DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)),
                            time: LocalTime.parse(values[Constants.PRICE_TIME]),
                            open: new Double(values[Constants.OPEN_PRICE].trim()),
                            high: new Double(values[Constants.HIGH_PRICE].trim()),
                            low: new Double(values[Constants.LOW_PRICE].trim()),
                            close: new Double(values[Constants.CLOSE_PRICE].trim())
                    )

                    prices += price
                }catch (Exception e) {
                    println "Exception at $lineNo $e"
                    throw e
                }
            }
        }

        def rows = dao.insert(prices)

        println "Successfully inserted $rows rows"
    }

    def size() {
        def count = dao.size()
        println "Size of DB: $count"
    }

    def list() {
        def prices = dao.list(10)
        prices.each {
            println it
        }
    }

    def truncate() {
        dao.truncate()
    }
}