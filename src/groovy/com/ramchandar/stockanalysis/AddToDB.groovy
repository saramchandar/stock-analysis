package com.ramchandar.stockanalysis

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddToDB {

    def DAO dao

    AddToDB() {
        dao = new DAO()
    }

    def void processFile(String fileName) {
        def prices = []
        new FileReader(fileName).eachLine { String line, lineNo ->
            if (lineNo != 1) {
                def values = line.split(Constants.FIELD_SEPARATOR)

                def price = new Price(
                        name: values[Constants.STOCK],
                        priceDate: LocalDate.parse(values[Constants.PRICE_DATE], DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)),
                        open: new Double(values[Constants.OPEN_PRICE].trim()),
                        high: new Double(values[Constants.HIGH_PRICE].trim()),
                        low: new Double(values[Constants.LOW_PRICE].trim()),
                        close: new Double(values[Constants.CLOSE_PRICE].trim())
                )

                prices += price
            }
        }

        def rows = dao.insert(prices)

        println "Successfully inserted $rows rows"
    }

    public static void main(String[] args) {
        new AddToDB().processFile("C:\\Misc\\Prices.txt")
    }
}