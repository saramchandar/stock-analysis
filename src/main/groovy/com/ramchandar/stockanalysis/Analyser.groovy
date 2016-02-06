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
    def
    static sql = Sql.newInstance("jdbc:postgresql://localhost:5432/NSEFUTURES", "postgres", "abc123", "org.postgresql.Driver")

    public static void main(String[] args) {
        def Analyser analyser = new Analyser()
        sql.eachRow("select distinct name, date from nsefutures") {
            println "Processing $it.name and $it.date "
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
        def mopen
        def longinit
        def longbuyinit = 'N'
        def longinittime
        def longbuyinittime
        def longbuyinitminute
        def longbuyprice
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
        longbuyinit = "N"
        longbuyprice = 0
        longtarget = 0
        longstpl = 0
        longpreviouslow = 0


        for (int i = 0; i < allPrices.size(); i++) {
            def singlePrice = allPrices[i]
            if (singlePrice.time == LocalTime.parse("09:45") && singlePrice.open < max && singlePrice.open > min) {
                longinit = 'Y'
            }
            if (singlePrice.time > LocalTime.parse("09:45") && singlePrice.high > max && longbuyinit == 'N') {
                longbuyinit = 'Y'
                longbuyinittime = singlePrice.time.hour
                longbuyinitminute = singlePrice.time.minute
                longbuyinitminute = longbuyinitminute + 1
            }
            if (singlePrice.time.hour == longbuyinittime && singlePrice.time.minute == longbuyinitminute && longbuyinit == "Y" && longbuyprice == 0) {
                longbuyprice = singlePrice.open
                longbuytime = singlePrice.time
                longstpl = longbuyprice - (1300 / (500000 / longbuyprice))
                if (singlePrice.low < longstpl) {
                    longstpl = singlePrice.low - 1
                }
                longtarget = longbuyprice + (1300 / (500000 / longbuyprice))
                println "Buy Time: $longbuytime ,Longbuy Price: $longbuyprice,STPL: $longstpl"
            }
            if (singlePrice.high > longtarget && longsaleprice == 0 && longbuyprice != 0) {
                longsaleprice = longtarget
                longsaletime = singlePrice.time
                println "Long Target acheived Time : $longsaletime, Sale Price: $longsaleprice"

            }
            if (longbuyprice != 0 && longsaleprice == 0 && singlePrice.low < longstpl && longbuyprice != 0) {
                longsaleprice = longstpl
                longsaletime = singlePrice.time
                println "STOP LOSS HIT. Longsale time : $longsaletime, Long Sale Price : $longsaleprice"

            }
            if (longbuyprice != 0 && longsaleprice != 0) {
                PandL = longsaleprice - longbuyprice
                println "Profit Loss : $PandL"
            }
            if (PandL > 0) {
                remark = "Target Achieved"
            }
            if (PandL < 0) {
                remark = "Stop Loss Hit"
            }
            println "Buy Price: $longbuyprice, Sale Price: $longsaleprice"
            if (longbuyprice > 0 && longsaleprice > 0) {
                println longbuyprice
                sql.execute("insert into NSETRADEBOOK (name, BUY_date, BUY_time, BUY_PRICE, SALE_DATE, SALE_TIME, SALE_PRICE, REMARK, PandL) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        stock, java.sql.Date.valueOf(date), java.sql.Time.valueOf(longbuytime), longbuyprice,
                        java.sql.Date.valueOf(singlePrice.priceDate), java.sql.Time.valueOf(longsaletime), longsaleprice, remark, PandL)
                break
            }
        }


        println "Sort Chances"

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
            if (singlePrice.time == LocalTime.parse("09:45") && singlePrice.open < max && singlePrice.open > min) {
                ssortinit = 'Y'
                println " Short init : $ssortinit"
            }
            if (singlePrice.time > LocalTime.parse("09:45") && singlePrice.low < min && ssortbuyinit == 'N') {
                ssortbuyinit = 'Y'
                sortbuyinittime = singlePrice.time.hour
                sortbuyinitminute = singlePrice.time.minute
                sortbuyinitminute = sortbuyinitminute + 1
                println "buyinit time : $sortbuyinittime,  $sortbuyinitminute, Buy init : $ssortbuyinit"
            }
            if (singlePrice.low < min - 1 && singlePrice.time > LocalTime.parse("09:44") && ssortbuyinit == 'Y') {
                sortsaletime = singlePrice.time
                sortsaleprice = (min - 1)
                sorttarget = sortsaleprice - (1300 / (500000 / sortsaleprice))
                sortstpl = sortsaleprice + (1300 / (500000 / sortsaleprice))
                if (singlePrice.high > sortstpl) {
                    sortstpl = singlePrice.high + 1
                }
                println " Sort Time : $sortsaletime, Sale Price : $sortsaleprice, Target: $sorttarget Stop Loss:$sortstpl "

            }
            if (singlePrice.low < sorttarget && sortbuyprice == 0 && sortsaleprice != 0) {
                sortbuyprice = sorttarget
                sortbuytime = singlePrice.time
                println "Sort Target acheived Time : $sortbuytime, Buy Price: $sortbuyprice"
            }
            if (sortsaleprice != 0 && singlePrice.high > sortstpl && sortbuyprice == 0) {
                sortbuyprice = longstpl
                sortbuytime = singlePrice.time
                println "STOP LOSS HIT. Sortbuy time : $sortbuytime, Sort buy Price : $sortbuyprice"
                break
            }
            if (sortbuyprice != 0 && sortsaleprice != 0) {
                sPandL = sortsaleprice - sortbuyprice
                println "Profit Loss : $PandL"
            }
            if (sPandL > 0) {
                remark = "Target Achieved"
            }
            if (sPandL < 0) {
                remark = "Stop Loss Hit"
            }

            if (sortsaleprice != 0) {
                for (i = 0; i < allPrices.size(); i++) {
                    singlePrice = allPrices[i]
                    if (singlePrice.low < sorttarget && singlePrice.time > sortsaletime) {
                        sortbuytime = singlePrice.time
                        sortbuyprice = singlePrice.low
                        sPandL = sortsaleprice - sortbuyprice
                    }
                }
                if (sortbuyprice != 0) {
                    println "Sort Buy time : $sortbuytime, Sort Buy Price : $sortbuyprice"
                    remark = "Sort Trade Ok"
                    sql.execute("insert into NSETRADEBOOK (name, BUY_date, BUY_time, BUY_PRICE, SALE_DATE, SALE_TIME, SALE_PRICE, REMARK, PandL) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            stock, java.sql.Date.valueOf(date), java.sql.Time.valueOf(sortbuytime), sortbuyprice,
                            java.sql.Date.valueOf(date), java.sql.Time.valueOf(sortsaletime), sortsaleprice, remark, sPandL)

                } else {
                    println "Sort Buy Not Possible"
                }
            }
        }

    }
}