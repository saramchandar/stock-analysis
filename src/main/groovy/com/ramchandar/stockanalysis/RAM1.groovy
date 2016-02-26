package com.ramchandar.stockanalysis1


/**
 * Created by S. A. Ramchandar on 2/23/2016.
 */

import com.ramchandar.stockanalysis.domain.DAO
import com.ramchandar.stockanalysis.domain.Price
import groovy.sql.Sql
import org.apache.log4j.Logger

import java.time.LocalDate
import java.time.LocalTime

class Ram {

    def static LOG = Logger.getLogger(Ram.class)

    def DAO dao

    Ram(){
        dao = new DAO()
    }
    def static sql = Sql.newInstance("jdbc:postgresql://localhost:5432/NSEFUTURES", "postgres", "abc123", "org.postgresql.Driver")

    public static void main(String[] args) {
        def Ram Ram = new Ram()
        def M_open = 0
        def M_high = 0
        def M_low = 0
        def M_volume = 0
        def M_close = 0
        def M_hour = 0
        def M_minute =0

        // sql.execute("truncate table nsetradebook")

        sql.eachRow("select  name,date,time, open,high,low,close, volume  from nsetable ") {
           // LOG.info("Processing $it.name and $it.date ")

           // Ram.process(it.name as String, LocalDate.parse(it.date as String))
           // Ram.process(it.name as String, LocalDate.parse(it.date as String))
           // List<Price> allPrices = dao.getPrices(stock, date)
            for (int i = 0; i < 376; i++) {
                // def singlePrice = allPrices[i]
                //if (it.time == M_time)

                    M_open = it.open
                    M_high = it.high
                    M_low = it.low
                    M_close = it.close
                    M_volume = it.volume
                    M_hour =it.time.hour
                    M_minute = it.time.minute





            }

                println " $it.time, O: $it.open H:$M_high, L : $M_low, C: $M_close, $M_hour, $M_minute,"

        }

    }

}