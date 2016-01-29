package com.ramchandar.stockanalysis

import com.ramchandar.stockanalysis.domain.DAO
import com.ramchandar.stockanalysis.domain.Price
import org.apache.log4j.Logger

import java.time.LocalDate

class Analyser {

    def static LOG = Logger.getLogger(Analyser.class)

    def DAO dao

    Analyser() {
        dao = new DAO()
    }

    def void process(String stock, LocalDate date) {
        LOG.info("Processing for $stock and $date")
        def minMax = dao.getMinMaxForFirstHalfHour(stock, date)
        LOG.info("Min is ${minMax[0]} Max is ${minMax[1]}")

        List<Price> prices = dao.getPrices(stock, date)

        prices.each {
            LOG.info(it)
        }
    }
}