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
        def min = minMax[0]
        def max = minMax[1]
        LOG.info("Min is ${min} Max is ${max}")

        List<Price> allPrices = dao.getPrices(stock, date)

        for(int i = 0; i < allPrices.size(); i++){
            def singlePrice = allPrices[i]
            println singlePrice
        }

    }
}