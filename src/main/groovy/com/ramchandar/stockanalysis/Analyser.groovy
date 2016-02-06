package com.ramchandar.stockanalysis

import com.ramchandar.stockanalysis.domain.DAO
import com.ramchandar.stockanalysis.domain.Price
import groovy.sql.Sql
import org.apache.log4j.Logger

import java.time.LocalDate
import java.time.LocalTime

class Analyser {

    def static LOG = Logger.getLogger(Analyser.class)

    def DAO dao

    Analyser() {
        dao = new DAO()
    }
    def static sql = Sql.newInstance("jdbc:postgresql://localhost:5432/NSEFUTURES", "postgres", "abc123", "org.postgresql.Driver")

    public static void main(String[] args) {
        def Analyser analyser = new Analyser()

        sql.execute("truncate table nsetradebook")

        sql.eachRow("select distinct name, date from nsefutures") {
            LOG.info("Processing $it.name and $it.date ")
            analyser.process(it.name as String, LocalDate.parse(it.date as String))
        }
    }

    def void process(String stock, LocalDate date) {
        LOG.info("Processing for $stock and $date")
        def minMax = dao.getMinMaxForFirstHalfHour(stock, date)
        def min = minMax[0]
        def max = minMax[1]
        LOG.info("Min is ${min} Max is ${max}")

        List<Price> allPrices = dao.getPrices(stock, date)
        def longbuyinit = 'N'
        def longbuyinittime
        def longbuyinitminute
        def longbuyprice = 0
        def longbuytime
        def longtarget
        def longstpl
        def longpreviouslow
        def RTime

        def mvolume = 0
        def sortinit = "N"
        def sortinittime
        def sortsaleinit
        def sortbuytarget
        def longsaletime
        def longsaleprice = 0
        def String remark
        def PandL = 0
        longtarget = 0
        longstpl = 0
        longpreviouslow = 0

        for (int i = 0; i < allPrices.size(); i++) {
            def singlePrice = allPrices[i]

            if (singlePrice.time > LocalTime.parse("09:45") && singlePrice.high > max && longbuyinit == 'N') {
                longbuyinit = 'Y'
                longbuyinittime = singlePrice.time.hour
                longbuyinitminute = singlePrice.time.minute
                longbuyinitminute = longbuyinitminute + 1
            }
            if (singlePrice.time.hour == longbuyinittime &&singlePrice.volume > 25000 && singlePrice.time.minute == longbuyinitminute && longbuyinit == "Y" && longbuyprice == 0) {
                longbuyprice = singlePrice.open
                longbuytime = singlePrice.time
                longstpl = longbuyprice - (1300 / (500000 / longbuyprice))
                if (singlePrice.low < longstpl) {
                    longstpl = singlePrice.low - 1
                }
                longtarget = longbuyprice + (1300 / (500000 / longbuyprice))
                LOG.debug("Buy Time: $longbuytime ,Longbuy Price: $longbuyprice,STPL: $longstpl")
            }
            if (singlePrice.high > longtarget && longsaleprice == 0 && longbuyprice != 0) {
                longsaleprice = longtarget
                longsaletime = singlePrice.time
                LOG.debug("Long Target acheived Time : $longsaletime, Sale Price: $longsaleprice")

            }
            if (longbuyprice != 0 && longsaleprice == 0 && singlePrice.low < longstpl && longbuyprice != 0) {
                longsaleprice = longstpl
                longsaletime = singlePrice.time
                LOG.debug("STOP LOSS HIT. Longsale time : $longsaletime, Long Sale Price : $longsaleprice")

            }
            if (longbuyprice != 0 && longsaleprice != 0) {
                PandL = longsaleprice - longbuyprice
                LOG.info("Profit Loss : $PandL")
            }
            if (PandL > 0) {
                remark = "Target Achieved"
            } else if (PandL < 0) {
                remark = "Stop Loss Hit"
            }
            LOG.debug("Buy Price: $longbuyprice, Sale Price: $longsaleprice")
            if (longbuyprice > 0 && longsaleprice > 0) {
                LOG.debug(longbuyprice)
                sql.execute("insert into NSETRADEBOOK (name, BUY_date, BUY_time, BUY_PRICE, SALE_DATE, SALE_TIME, SALE_PRICE, REMARK, PandL) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        stock, java.sql.Date.valueOf(date), java.sql.Time.valueOf(longbuytime), longbuyprice,
                        java.sql.Date.valueOf(singlePrice.priceDate), java.sql.Time.valueOf(longsaletime), longsaleprice, remark, PandL)
                break
            }
            if (longbuyprice == 0 && longsaleprice == 0){
                println "No Long Trade possible"
                break
            }
        }


        LOG.info("Sort Chances")

        // Sort chances
        def sortbuytime, sortbuyprice, sorttarget, sortsaletime, sortsaleprice, sPandL, sortstpl, ssortinit, sortbuyinittime, sortbuyinitminute
        def ssortbuyinit, sortsaleinittime, sortsaleinitminute
        ssortinit = 'N'
        ssortbuyinit = 'N'
        sortbuyinittime = 0
        sorttarget = 0
        sortsaletime = LocalTime.parse("00:00")
        sortsaleprice = 0
        sortbuyprice = 0
        sPandL = 0
        sortstpl = 0

        for (int i = 0; i < allPrices.size(); i++) {
            def singlePrice = allPrices[i]

            if (singlePrice.time > LocalTime.parse("09:45") && singlePrice.low < min && ssortbuyinit == 'N') {
                ssortbuyinit = 'Y'
                sortbuyinittime = singlePrice.time.hour
                sortbuyinitminute = singlePrice.time.minute
                sortbuyinitminute = sortbuyinitminute + 1
                LOG.info("buyinit time : $sortbuyinittime,  $sortbuyinitminute, Buy init : $ssortbuyinit")
            }
            if (singlePrice.low < min - 1 && singlePrice.time > LocalTime.parse("09:44") && ssortbuyinit == 'Y') {
                sortsaletime = singlePrice.time
                sortsaleprice = (min - 1)
                sorttarget = sortsaleprice - (1300 / (500000 / sortsaleprice))
                sortstpl = sortsaleprice + (1300 / (500000 / sortsaleprice))
                if (singlePrice.high > sortstpl) {
                    sortstpl = singlePrice.high + 1
                }
                LOG.info(" Sort Time : $sortsaletime, Sale Price : $sortsaleprice, Target: $sorttarget Stop Loss:$sortstpl ")
            }
            if (singlePrice.low < sorttarget && sortbuyprice == 0 && sortsaleprice != 0) {
                sortbuyprice = sorttarget
                sortbuytime = singlePrice.time
                LOG.info("Sort Target acheived Time : $sortbuytime, Buy Price: $sortbuyprice")
                break
            }
            if (sortsaleprice != 0 && singlePrice.high > sortstpl && sortbuyprice == 0) {
                sortbuyprice = longstpl
                sortbuytime = singlePrice.time
                LOG.info("STOP LOSS HIT. Sortbuy time : $sortbuytime, Sort buy Price : $sortbuyprice")
                break
            }

            LOG.debug("Sort Buy Price: $sortbuyprice")

        }

        if (sortbuyprice != 0 && sortsaleprice != 0) {
            sPandL = sortsaleprice - sortbuyprice
            LOG.info("Profit Loss : $PandL")
        }
        if (sPandL > 0) {
            remark = "Target Achieved"
        }
        if (sPandL < 0) {
            remark = "Stop Loss Hit"
        }

        if (sortbuyprice != 0) {
            LOG.info("Sort Buy time : $sortbuytime, Sort Buy Price : $sortbuyprice")
            remark = "Sort Trade Ok"
            sql.execute("insert into NSETRADEBOOK (name, BUY_date, BUY_time, BUY_PRICE, SALE_DATE, SALE_TIME, SALE_PRICE, REMARK, PandL) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    stock, java.sql.Date.valueOf(date), java.sql.Time.valueOf(sortbuytime), sortbuyprice,
                    java.sql.Date.valueOf(date), java.sql.Time.valueOf(sortsaletime), sortsaleprice, remark, sPandL)

        } else {
            LOG.info("Sort Buy Not Possible")
        }
    }
}