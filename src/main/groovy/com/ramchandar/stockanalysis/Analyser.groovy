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
        def Analyser Analyser = new Analyser()

        sql.execute("truncate table nsetradebook")

        sql.eachRow("select distinct name, date from nsefutures where date = '2016-02-23' ") {
            LOG.info("Processing $it.name and $it.date ")
            Analyser.process(it.name as String, LocalDate.parse(it.date as String))

        }
    }

    def void process(String stock, LocalDate date) {
        // LOG.info("Processing for $stock and $date")
        // def minMax = dao.getMinMaxForFirstHalfHour(stock, date)
        // def min = minMax[0]
        // def max = minMax[1]
        // LOG.info("Min is ${min} Max is ${max}")
        def m_max = 0
        def m_min = 0
        def m_hour = 0
        def m_minute =0
        List<Price> allPrices = dao.getPrices(stock, date)

         for (int i = 0; i < allPrices.size(); i++) { // FOR NEXT I BEGIN
            def singlePrice = allPrices[i]


             if (m_max == 0 && m_min== 0){
                 m_max = singlePrice.high
                 m_min = singlePrice.low
                // println "max $m_max, min : $m_min"
             }
             if (m_max < singlePrice.high){
                 m_max = singlePrice.high
             }
             if (m_min > singlePrice.low){
                 m_min = singlePrice.low
             }
             if (singlePrice.time == LocalTime.parse("09:40")) {
                 break
             }
        } // FOR NEXT I ENDS
             println "Max : $m_max, Min : $m_min"
        println ' Type III starts'
        def typeIIItrade = 'N'
        def typeIIItradetime = LocalTime.parse('00:00')
        def typeIIIinit ='N'
        def typeIIIbuyinit = 'N'
        def typeIIIbuyinittime
        def typeIIIbuyinitminute
        def typeIIIbuyprice = 0
        def typeIIIbuytime = LocalTime.parse("00:00")
        def typeIIIsaleprice = 0
        def typeIIIsaletime = LocalTime.parse("00:00")
        def typeIIItarget = 0
        def typeIIIstpl = 0
        def String typeIIIremark
        def typeIIImclose = 0
        def typeIIIPandL = 0
        def String remark
        remark = ' '

        for(int i = 0; i < allPrices.size(); i++) { // FOR NEXT TYPE III BEGIN
            def singlePrice = allPrices[i]

            if (singlePrice.time == LocalTime.parse("09:45") && singlePrice.open < m_max && singlePrice.open > m_min) {
                typeIIIinit = 'Y'
                println "typeIIIinit : $typeIIIinit"
            }
            if (singlePrice.time > LocalTime.parse("09:45") && typeIIIinit == 'Y' && singlePrice.low < m_min && typeIIItrade == 'N') {
                    typeIIItrade = 'Y'
                    typeIIItradetime = singlePrice.time
                println " typeIIItrade : $typeIIItrade, Time : $typeIIItradetime  Low : $singlePrice.low $m_min"
            }
            if (singlePrice.time > typeIIItradetime && typeIIIinit == 'Y' && typeIIItrade == 'Y' && typeIIIbuyinit == 'N'&& singlePrice.high > m_min  ) {
                typeIIIbuyinit = 'Y'
                typeIIIbuyinittime = singlePrice.time.hour
                typeIIIbuyinitminute = singlePrice.time.minute
                typeIIIbuyinitminute = typeIIIbuyinitminute + 5
                println " typeIIIinit : $typeIIIinit, typeIIItrade : $typeIIItrade, typeIIIbuyinit : $typeIIIbuyinit, "
                println " time : $singlePrice.time, typeIIIbuyinitminute : $typeIIIbuyinitminute"

            }
            def query = "select  lot from nselots where name =  $stock"
            // println stock
            def s_lot = 0
            sql.eachRow(query) {
                s_lot = it.lot
            }
            if (singlePrice.time.hour == typeIIIbuyinittime && singlePrice.time.minute == typeIIIbuyinitminute && singlePrice.time < LocalTime.parse('12:00') && typeIIItrade == 'Y' && typeIIIbuyinit == "Y" && typeIIIbuyprice == 0) {
                typeIIIbuyprice = singlePrice.open
                typeIIIbuytime = singlePrice.time
                typeIIIstpl = typeIIIbuyprice - (3000 / s_lot)

                typeIIItarget = typeIIIbuyprice + (1500 / s_lot)
                 println " LSTPL : $typeIIIstpl, Low: $singlePrice.low, Type III Target : $typeIIItarget "
                 LOG.info("Buy Time: $typeIIIbuytime ,Type III buy Price: $typeIIIbuyprice,Type III STPL: $typeIIIstpl")
            }
            if (singlePrice.time < LocalTime.parse('12:31') && singlePrice.high >= typeIIItarget && typeIIIsaleprice == 0 && typeIIIbuyprice != 0) {
                typeIIIsaleprice = typeIIItarget
                typeIIIsaletime = singlePrice.time
                 //LOG.info("Type III Target acheived Time : $typeIIIsaletime, Sale Price: $typeIIIsaleprice")

            }
            if (typeIIIbuyprice != 0 && typeIIIsaleprice == 0 && singlePrice.low < typeIIIstpl) {
                typeIIIsaleprice = typeIIIstpl
                typeIIIsaletime = singlePrice.time
               // LOG.debug("Tyep III STOP LOSS HIT. typeIIIsale time : $typeIIIsaletime, Type III Sale Price : $typeIIIsaleprice")

            }
            if (singlePrice.time == LocalTime.parse("12:30") && typeIIIbuyprice !=0 && typeIIIsaleprice == 0) {
                typeIIImclose = singlePrice.close
                typeIIIsaletime = singlePrice.time
            }


        } //FOR NEXT TYPE III END
        if (typeIIIbuyprice !=0 && typeIIIsaleprice == 0){
            typeIIIsaleprice = typeIIImclose

        }
        if (typeIIIbuyprice != 0 && typeIIIsaleprice != 0) {
            // println "LBuy : $typeIIIbuyprice, LSale : $typeIIIsaleprice"
            typeIIIPandL = typeIIIsaleprice - typeIIIbuyprice
            // LOG.info("Type III Profit Loss : $typeIIIPandL")
        }
        if (typeIIIPandL > 0) {
            remark = " TYPE III LONG Target Achieved"
        } else if (typeIIIPandL < 0) {
            remark = " TYPE III LONG Stop Loss Hit"
        }
        if (typeIIIsaletime == LocalTime.parse('12:30')){
            remark = " TYPE III LONG CLOSED @ 12:30"
        }
        // LOG.debug("Buy Price: $longbuyprice, Sale Price: $longsaleprice")
        if (typeIIIbuyprice > 0 && typeIIIsaleprice > 0) {
            //LOG.info("Long buy time: $longbuytime, Long Sale Time : $longsaletime")
            sql.execute("insert into NSETRADEBOOK (name, BUY_date, BUY_time, BUY_PRICE, SALE_DATE, SALE_TIME, SALE_PRICE, REMARK, PandL) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    stock, java.sql.Date.valueOf(date), java.sql.Time.valueOf(typeIIIbuytime), typeIIIbuyprice,
                    java.sql.Date.valueOf(date), java.sql.Time.valueOf(typeIIIsaletime), typeIIIsaleprice, remark, typeIIIPandL)
        } else {
            remark = " Type III LONG trade not initiated"

            sql.execute("insert into NSETRADEBOOK (name, BUY_date, BUY_time, BUY_PRICE, SALE_DATE, SALE_TIME, SALE_PRICE, REMARK, PandL) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    stock, java.sql.Date.valueOf(date), java.sql.Time.valueOf(typeIIIbuytime), typeIIIbuyprice,
                    java.sql.Date.valueOf(date), java.sql.Time.valueOf(typeIIIsaletime), typeIIIsaleprice, remark, typeIIIPandL)
        }
        println "Process of Type III ends"
        println " Process of Type I begings"
        def typeIinit = 'N'
        def longbuyinit = 'N'
        def longbuyinittime
        def longbuyinitminute
        def longbuyprice = 0
        def longbuytime = LocalTime.parse("00:00")
        def longsaletime = LocalTime.parse("00:00")
        def longtarget = 0
        def longstpl = 0

        remark = ' '
        def mclose = 0


        def longsaleprice = 0
        def LPandL = 0
        def sclose = 0

            for(int i = 0; i < allPrices.size(); i++) { // FOR NEXT II BEGIN
                def singlePrice = allPrices[i]
                // println $singlePrice.time
                if (singlePrice.time == LocalTime.parse("09:45") && singlePrice.open < m_max && singlePrice.open > m_min) {
                    typeIinit = 'Y'

                }

                    if (singlePrice.time > LocalTime.parse("09:45") && typeIinit == 'Y' && singlePrice.high > m_max && longbuyinit == 'N') {
                        longbuyinit = 'Y'
                        longbuyinittime = singlePrice.time
                        longbuyinitminute = singlePrice.time.minute
                        longbuyinitminute = longbuyinitminute + 1
                         println "Long buy init : $longbuyinit, $longbuyinittime "
                    }

                    def query = "select  lot from nselots where name =  $stock"
                    // println stock
                    def s_lot = 0
                    sql.eachRow(query) {
                        s_lot = it.lot
                    }

                    if (singlePrice.time > longbuyinittime  && longbuyinit == "Y" && longbuyprice == 0 && singlePrice.close > m_max && singlePrice.ema5 < m_max ) {
                        longbuyprice = singlePrice.open
                        longbuytime = singlePrice.time
                        longstpl = longbuyprice - (1500 / s_lot)
                        // println " LSTPL : $longstpl, Low: $singlePrice.low "
                        longtarget = longbuyprice + (1500 / s_lot)
                       println "Buy Time: $longbuytime ,Longbuy Price: $longbuyprice,1/2 HR MIN: $m_max, ema5 : $singlePrice.ema5, ema20 :$singlePrice.ema20"
                    }
                    if (singlePrice.high >= longtarget && longsaleprice == 0 && longbuyprice != 0) {
                        longsaleprice = longtarget
                        longsaletime = singlePrice.time
                       //  LOG.debug("Long Target acheived Time : $longsaletime, Sale Price: $longsaleprice")

                    }
                    // if (longbuyprice != 0 && longsaleprice == 0 && singlePrice.low < longstpl) {
                    //    longsaleprice = longstpl
                    //    longsaletime = singlePrice.time
                        //LOG.debug("STOP LOSS HIT. Longsale time : $longsaletime, Long Sale Price : $longsaleprice")

                    //}
                    if (singlePrice.time == LocalTime.parse("15:20") && longbuyprice !=0 && longsaleprice == 0 ) {
                        longsaleprice = singlePrice.close
                        longsaletime = singlePrice.time
                    }


            } // FOR NEXT II ENDS

            if (longbuyprice !=0 && longsaleprice == 0){
                longsaleprice = mclose
            }

            if (longbuyprice != 0 && longsaleprice != 0) {
                // println "LBuy : $longbuyprice, LSale : $longsaleprice"
                LPandL = longsaleprice - longbuyprice
                // LOG.info("Profit Loss : $LPandL")
            }
            if (LPandL > 0) {
                remark = " TYPE I LONG Target Achieved"
            } else if (LPandL < 0) {
                remark = " TYPE I LONG Stop Loss Hit"
            }
                // LOG.debug("Buy Price: $longbuyprice, Sale Price: $longsaleprice")

            if (longbuyprice > 0 && longsaleprice > 0) {
                // LOG.info("Long buy time: $longbuytime, Long Sale Time : $longsaletime")
                sql.execute("insert into NSETRADEBOOK (name, BUY_date, BUY_time, BUY_PRICE, SALE_DATE, SALE_TIME, SALE_PRICE, REMARK, PandL) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        stock, java.sql.Date.valueOf(date), java.sql.Time.valueOf(longbuytime), longbuyprice,
                        java.sql.Date.valueOf(date), java.sql.Time.valueOf(longsaletime), longsaleprice, remark, LPandL)
            } else {
                remark = " Type I trade not initiated"
                sql.execute("insert into NSETRADEBOOK (name, BUY_date, BUY_time, BUY_PRICE, SALE_DATE, SALE_TIME, SALE_PRICE, REMARK, PandL) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        stock, java.sql.Date.valueOf(date), java.sql.Time.valueOf(longbuytime), longbuyprice,
                        java.sql.Date.valueOf(date), java.sql.Time.valueOf(longsaletime), longsaleprice, remark, LPandL)
        }
        println "Type I process ends"
        println "Type II process begins"
        // LOG.info("Sort Chances")

        // Sort chances
        def sortbuytime, sortbuyprice, sorttarget, sortsaletime, sortsaleprice, sPandL, sortstpl, typeIIinit, sortsaleinittime, sortsaleinitminute
        def ssortsaleinit
        typeIIinit = 'N'
        ssortsaleinit = 'N'
        sortsaleinittime = LocalTime.parse("00:00")
        sortbuytime = LocalTime.parse("00:00")
        sorttarget = 0
        sortsaletime = LocalTime.parse("00:00")
        sortsaleprice = 0
        sortbuyprice = 0
        sPandL = 0
        sortstpl = 0
        remark = ' '


        for (int i = 0; i < allPrices.size(); i++) {  // FOR NEXT III BEGIN
            def singlePrice = allPrices[i]

            if (singlePrice.time == LocalTime.parse("09:45") && singlePrice.open < m_max && singlePrice.open > m_min) {
                typeIIinit = 'Y'

            }
            if (singlePrice.time > LocalTime.parse("09:45") && typeIIinit == 'Y' && singlePrice.low < m_min && ssortsaleinit == 'N') {
                ssortsaleinit = 'Y'
                sortsaleinittime = singlePrice.time
                sortsaleinitminute = singlePrice.time.minute
                sortsaleinitminute = sortsaleinitminute + 4
                LOG.info(" Saleinit time : $sortsaleinittime,  $sortsaleinitminute, Sale init : $ssortsaleinit")
            }
            def query = "select  lot from nselots where name =  $stock"
            def n_lot = 0
            sql.eachRow(query) {
                n_lot = it.lot

            }

            if (  singlePrice.time > sortsaleinittime && ssortsaleinit == 'Y' && sortsaleprice == 0 && singlePrice.close < m_min && singlePrice.ema5 < m_min) {
                sortsaletime = singlePrice.time
                sortsaleprice = singlePrice.open
                sorttarget = sortsaleprice - (1500/ n_lot)
                sortstpl = sortsaleprice + (1500/ n_lot)
                //LOG.info(" Sort Time : $sortsaletime, Sale Price : $sortsaleprice, Target: $sorttarget Stop Loss:$sortstpl ")
            }
            if (singlePrice.low <= sorttarget && sortbuyprice == 0 && sortsaleprice != 0) {
                sortbuyprice = sorttarget
                sortbuytime = singlePrice.time
                // LOG.info("Sort Target acheived Time : $sortbuytime, Buy Price: $sortbuyprice")
                break
            }
            // if (sortsaleprice != 0 && singlePrice.high < sortstpl  && sortbuyprice == 0) {
            //    sortbuyprice = sortstpl
            //    sortbuytime = singlePrice.time
             //   LOG.info("STOP LOSS HIT. Sortbuy time : $sortbuytime, Sort buy Price : $sortbuyprice")
            //    break
            //}

            if (singlePrice.time == LocalTime.parse("15:20") && sortsaleprice != 0 && sortbuyprice == 0){
                sortbuyprice = singlePrice.close
                sortbuytime = singlePrice.time

            }
        } // FOR NEXT III ENDS

            if (sortbuyprice !=0 && sortsaleprice !=0){
                sPandL = sortsaleprice - sortbuyprice
                // LOG.info(" Short Profit / Loss : $sPandL")
            }
            if (sPandL > 0) {
                remark = " TYPE II SORT Target Achieved"
            }
            if (sPandL < 0) {
                remark = " TYPE II SORT Stop Loss Hit"
            }
            if (sortbuytime == LocalTime.parse('12:30'))[
                remark = " TYPE II TRADE CLOSED @ 12:30"
            ]
            if (sortbuyprice != 0) {
                // LOG.info("Sort Buy time : $sortbuytime, Sort Buy Price : $sortbuyprice")
                sql.execute("insert into NSETRADEBOOK (name, BUY_date, BUY_time, BUY_PRICE, SALE_DATE, SALE_TIME, SALE_PRICE, REMARK, PandL) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        stock, java.sql.Date.valueOf(date), java.sql.Time.valueOf(sortbuytime), sortbuyprice,
                        java.sql.Date.valueOf(date), java.sql.Time.valueOf(sortsaletime), sortsaleprice, remark, sPandL)

            } else {
                remark = ' TYPE II SORT Trade not initiated'
                sql.execute("insert into NSETRADEBOOK (name, BUY_date, BUY_time, BUY_PRICE, SALE_DATE, SALE_TIME, SALE_PRICE, REMARK, PandL) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        stock, java.sql.Date.valueOf(date), java.sql.Time.valueOf(sortbuytime), sortbuyprice,
                        java.sql.Date.valueOf(date), java.sql.Time.valueOf(sortsaletime), sortsaleprice, remark, sPandL)

            }
            println " Type II process ends"

    }
}
