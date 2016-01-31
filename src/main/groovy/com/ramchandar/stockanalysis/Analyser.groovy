package com.ramchandar.stockanalysis

import com.ramchandar.stockanalysis.domain.DAO
import com.ramchandar.stockanalysis.domain.Price
import org.apache.log4j.Logger

import java.time.LocalDate
import java.time.LocalTime

class Analyser {

    def static LOG = Logger.getLogger(Analyser.class)

    def DAO dao

    Analyser() {
        dao = new DAO()
    }

    public static void main(String[] args) {
        new Analyser().process("BANKNIFTY_F1", LocalDate.parse("2015-04-01"))
    }

    def void process(String stock, LocalDate date) {
        LOG.info("Processing for $stock and $date")
        def minMax = dao.getMinMaxForFirstHalfHour(stock, date)
        def min = minMax[0]
        def max = minMax[1]
        LOG.info("Min is ${min} Max is ${max}")

        List<Price> allPrices = dao.getPrices(stock, date)
        def longbuyprice = 0
        def longbuytime
        def longtarget = 0
        def longsaletime
        def longsaleprice = 0
        for (int i = 0; i < allPrices.size(); i++) {
            def singlePrice = allPrices[i]
            if (singlePrice.high > max && singlePrice.time > LocalTime.parse("09:44")) {
                longbuyprice = singlePrice.high
                if (longbuyprice + 1 > max) {
                    longbuyprice = max + 1
                    longbuytime = singlePrice.time
                    longtarget = longbuyprice + 40
                    println "Buy Time : $longbuytime, Buy Prince : $longbuyprice Target amount : $longtarget"
                    break
                }
            }
        }
        if (longbuyprice == 0) {
            println "No Long Buy Trade Possible"
        }
        if (longbuyprice != 0) {
            for (int i = 0; i < allPrices.size(); i++) {
                def singlePrice = allPrices[i]
                if (singlePrice.high > longtarget && singlePrice.time > longbuytime) {
                    longsaletime = singlePrice.time
                    longsaleprice = singlePrice.high
                    println "Sale Time : $longsaletime, Sale Price : $longsaleprice"
                    break
                }

            }
            if (longsaleprice == 0) {
                println "No Long Sale Trade possible"
            }
        }
        println "Sort Chances"

        // Sort chances
        def sortbuytime, sortbuyprice, sorttarget, sortsaletime, sortsaleprice
        sortbuyprice = 0
        sorttarget = 0
        sortsaleprice = 0

        for (int i = 0; i < allPrices.size(); i++) {
            def singlePrice = allPrices[i]
            if (singlePrice.low < min - 1 && singlePrice.time > LocalTime.parse("09:44")) {
                sortsaletime = singlePrice.time
                sortsaleprice = (min-1)
                sorttarget = sortsaleprice-40
                break
            }
        }
        if (sortsaleprice == 0) {
            println "No Sort Trade Possible"
        } else {
            println "Sort Sale time : $sortsaletime, Sort Sale Price : $sortsaleprice, Sort Buy target : $sorttarget"
        }
        if (sortsaleprice != 0) {
            for (int i = 0; i < allPrices.size(); i++) {
                def singlePrice = allPrices[i]
                if (singlePrice.low < sorttarget && singlePrice.time > sortsaletime){
                    sortbuytime = singlePrice.time
                    sortbuyprice = singlePrice.low
                }
            }
            if (sortbuyprice != 0){
                println "Sort Buy time : $sortbuytime, Sort Buy Price : $sortbuyprice"
            }
            else {
                println "Sort Buy Not Possible"
            }
        }
    }
}