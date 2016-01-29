package com.ramchandar.stockanalysis.domain

import groovy.sql.Sql
import org.apache.commons.lang3.Validate
import org.apache.log4j.Logger

import java.time.LocalDate

class DAO {

    def static LOG = Logger.getLogger(DAO.class)

    def Sql sql

    DAO() {
        sql = Sql.newInstance("jdbc:postgresql://localhost:5432/NSEFUTURES", "postgres", "abc123", "org.postgresql.Driver")
    }

    def Integer insert(List<Price> prices) {
        Validate.notNull(prices)
        Validate.noNullElements(prices)

        prices.each {
            sql.execute("insert into NSEFUTURES (name, date, time, open, high, low, close, volume) values (?, ?, ?, ?, ?, ?, ?, ?)",
                    it.name, java.sql.Date.valueOf(it.priceDate), java.sql.Time.valueOf(it.time), it.open, it.high, it.low, it.close, it.volume)
        }

        prices.size()
    }

    def size() {
        sql.firstRow("select count(*) as total_count from NSEFUTURES").total_count
    }

    private List<Price> query(String query) {
        def prices = []
        sql.eachRow(query) {
            prices += Price.rowMapperClosure(it)
        }
        prices
    }

    def list(Integer rowCount) {
        def queryString = "select name, date, time, open, high, low, close, volume from NSEFUTURES limit $rowCount"
        LOG.debug("Query for List: $queryString")
        query(queryString)
    }

    List<Price> getPrices(String stock, LocalDate date) {
        def queryString = "select name, date, time, open, high, low, close, volume " +
                "from NSEFUTURES " +
                "where name = '$stock' and date = '$date'"
        LOG.debug("Query for getPrices: $queryString")
        query(queryString)
    }

    def truncate() {
        sql.execute("truncate table NSEFUTURES")
    }

    def List getMinMaxForFirstHalfHour(String stock, LocalDate date) {
        def query = "select min(high) as min, max(high) as max " +
                "from nsefutures " +
                "where time > '09:15' and time <= '09:44' " +
                "and date = '$date' and name = '$stock'"
        LOG.debug(query)
        def min, max
        sql.eachRow(query) {
            min = it.min
            max = it.max
        }
        [min, max]
    }
}