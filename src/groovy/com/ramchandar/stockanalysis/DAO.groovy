package com.ramchandar.stockanalysis

import groovy.sql.Sql
import org.apache.commons.lang3.Validate

import java.time.LocalDate
import java.time.ZoneId

class DAO {

    def Sql sql

    DAO() {
        sql = Sql.newInstance("jdbc:postgresql://localhost:5432/NSEFUTURES", "postgres", "abc123", "org.postgresql.Driver")
    }

    def Integer insert(List<Price> prices) {
        Validate.notNull(prices)
        Validate.noNullElements(prices)

        prices.each {
            sql.execute("insert into NSEFUTURES (Name, Date, Open, High, Low, Close, Volume) values (?, ?, ?, ?, ?, ?, ?)",
                    it.name, java.sql.Date.valueOf(it.priceDate), it.open, it.high, it.low, it.close, it.volume)
        }

        prices.size()
    }

    def size() {
        sql.firstRow("select count(*) as total_count from NSEFUTURES").total_count
    }

    def list(Integer rowCount) {
        def prices = []
        sql.eachRow("select Name, Date, Open, High, Low, Close, Volume from NSEFUTURES") {
            def price = new Price(
                    name: it.Name,
                    priceDate: it.Date.toLocalDate(),
                    open: it.Open as Double,
                    high: it.High as Double,
                    low: it.Low as Double,
                    close: it.Close as Double,
                    volume: it.Volume as Long
            )
            prices += price
        }
        prices
    }

    def truncate() {
        sql.execute("truncate table NSEFUTURES")
    }
}