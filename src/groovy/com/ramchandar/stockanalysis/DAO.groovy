package com.ramchandar.stockanalysis

import groovy.sql.Sql
import org.apache.commons.lang3.Validate

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
}